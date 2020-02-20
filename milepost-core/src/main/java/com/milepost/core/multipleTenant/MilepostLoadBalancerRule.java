package com.milepost.core.multipleTenant;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.client.config.IClientConfig;
import com.netflix.discovery.EurekaClient;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ruifu Hua on 2020/1/8.
 * Robin默认使用的是com.netflix.loadbalancer.RoundRobinRule，本类也是从RoundRobinRule中复制过来的
 * 这里自定义一个，以实现租户、权重、标签等负载机制
 */
@Component//默认是单例的
@Primary //当有多个bean存在时，这个是主bean，优先使用这个
@Scope("prototype")//必须用原型的，否则getLoadBalancer()方法返回的LoadBalancer是最后一个FeignClient的LoadBalancer，因为被覆盖了。
public class MilepostLoadBalancerRule extends AbstractLoadBalancerRule {
    private AtomicInteger nextServerCyclicCounter;
    private static final boolean AVAILABLE_ONLY_SERVERS = true;
    private static final boolean ALL_SERVERS = false;

    private static Logger log = LoggerFactory.getLogger(RoundRobinRule.class);

    public static final String TENANT = "tenant";
    public static final String WEIGHT = "weight";
    public static final String LABEL_AND = "label-and";
    public static final String LABEL_OR = "label-or";
    public static final String TRACK_SAMPLING = "track-sampling";

    /**
     * 用来获取当前服务实例的InstanceInfo，
     * 在EurekaServer的控制台调用EurekaServer的rest接口改变服务实例元数据之后，
     * EurekaClient获取不到被改变的数据，必须使用org.springframework.cloud.client.discovery.DiscoveryClient才能获取到，
     * 考虑到性能问题，将EurekaServer控制台的动态改变多租户相关数据的功能去掉了。
     */
    @Autowired
    private EurekaClient eurekaClient;

    public MilepostLoadBalancerRule() {
        nextServerCyclicCounter = new AtomicInteger(0);
    }

    public MilepostLoadBalancerRule(ILoadBalancer lb) {
        this();
        setLoadBalancer(lb);
    }

    /**
     * RoundRobinRule类原本的方法
     * @param lb
     * @param key
     * @return
     */
    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        }

        Server server = null;
        int count = 0;
        while (server == null && count++ < 10) {
            //可达server
            List<Server> reachableServers = lb.getReachableServers();
            //所有server
            List<Server> allServers = lb.getAllServers();
            //可达server个数
            int upCount = reachableServers.size();
            //所有server个数
            int serverCount = allServers.size();
            //没有可达服务
            if ((upCount == 0) || (serverCount == 0)) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }

            int nextServerIndex = incrementAndGetModulo(serverCount);
            server = allServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }

            // Next.
            server = null;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + lb);
        }
        return server;
    }
    /**
     * Inspired by the implementation of {@link AtomicInteger#incrementAndGet()}.
     *
     * @param modulo The modulo to bound the value of the counter.
     * @return The next value.
     */
    private int incrementAndGetModulo(int modulo) {
        for (;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next))
                return next;
        }
    }

    /**
     * 自定义的选择服务方法，根据租户、权重、标签选择，RoundRobinRule类原本的方法复制过来的
     * @param lb
     * @param key
     * @return
     */
    public Server chooseByMilepost(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        }

        Server server = null;
        int count = 0;
        //重试10次，防止当前获取的可达服务在得到结果时候由于某种原因变成不可达了
        while (server == null && count++ < 10) {
            //可达server
            List<Server> reachableServers = lb.getReachableServers();
            //所有server
            List<Server> allServers = lb.getAllServers();
            //可达server个数
            int upCount = reachableServers.size();
            //所有server个数
            int serverCount = allServers.size();
            //没有可达服务
            if ((upCount == 0) || (serverCount == 0)) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }

            //获取当前服务租户、标签
            InstanceInfo currServerInstanceInfo = eurekaClient.getApplicationInfoManager().getInfo();

            //过滤租户
            reachableServers = tenantFilter(currServerInstanceInfo, reachableServers);
            if(reachableServers.isEmpty()){
                log.error("Server not found for tenant \""+ currServerInstanceInfo.getMetadata().get(TENANT) +"\"");
                return null;
            }

            //过滤标签，
            reachableServers = labelFilter(currServerInstanceInfo, reachableServers);
            if(reachableServers.isEmpty()){
                log.error("Server not found for label_or \""+ currServerInstanceInfo.getMetadata().get(LABEL_OR)
                        +"\" and label_and \""+ currServerInstanceInfo.getMetadata().get(LABEL_AND) +"\"");
                return null;
            }

            //权重
            server = chooseByWeight(reachableServers);

            if (server == null) {
                log.error("Server not found by weight, maybe all the servers were seted to zero weight。");
                return null;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }

            // Next.
            server = null;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + lb);
        }
        return server;
    }

    /**
     * 通过权重选择服务，<br>
     * 一个服务被选中的概率是这个服务的权重/所有服务权重之和，<br>
     * 权重为0的服务不会被选中，<br>
     * @param reachableServers
     * @return
     */
    private Server chooseByWeight(List<Server> reachableServers) {
        //reachableServers中服务的索引，每个服务索引的个数就是这个服务的权重值
        List<Integer> reachableServerIndexList = new ArrayList<>();
        //所有服务的权重之和
        Integer weightSum = 0;
        for(int i=0; i<reachableServers.size(); i++){
            Server server = reachableServers.get(i);
            DiscoveryEnabledServer discoveryEnabledServer = (DiscoveryEnabledServer)server;
            //获取服务权重值，是0和正整数
            String weightStr = discoveryEnabledServer.getInstanceInfo().getMetadata().get(WEIGHT);
            Integer weightInt = StringUtils.isBlank(weightStr)? 0 : Integer.parseInt(weightStr);
            for(int j=0; j<weightInt; j++){
                reachableServerIndexList.add(i);
            }
            weightSum = weightSum + weightInt;
        }

        if(reachableServerIndexList.isEmpty()){
            //所有服务的权重值都为0
            return null;
        }

        Random random = new Random();
        //生成一个0(包括)--weightSum(不包括)的随机整数
        int reachableServerIndexListRandomIndex = random.nextInt(weightSum);
        //用这个随机整数从reachableServerIndexList中取出一个索引
        int reachableServerIndex = reachableServerIndexList.get(reachableServerIndexListRandomIndex);
        //返回服务
        return reachableServers.get(reachableServerIndex);
    }

    /**
     * 过滤标签，<br>
     * 或标签：交集不为空，就选中服务实例，<br>
     * 与标签：两个服务实例的标签完全相等才选中服务实例，即标签集合中元素个数相等，元素相等，不区分顺序。<br>
     * 如果当前服务同时设置了或标签和与标签，则以或标签为准，忽略与标签，因为或标签过滤结果为空时，与标签一定为空。<br>
     * 如果当前服务未设置或标签，也未设置与标签，则他可以选择设置或未设置标签的所有服务。<br>
     *
     * @see MultipleTenantProperties
     *
     * @param currServerInstanceInfo
     * @param reachableServers
     * @return
     */
    private List<Server> labelFilter(InstanceInfo currServerInstanceInfo, List<Server> reachableServers) {
        List<Server> result = new ArrayList<>();
        //获取当前服务或标签
        String currLabelOr = currServerInstanceInfo.getMetadata().get(LABEL_OR);
        String currLabelAnd = currServerInstanceInfo.getMetadata().get(LABEL_AND);
        if(StringUtils.isNotBlank(currLabelOr)){
            //优先采用或标签
            List<String> currLabelOrList = new ArrayList<>(Arrays.asList(currLabelOr.split(",")));
            for(Server server : reachableServers){
                DiscoveryEnabledServer discoveryEnabledServer = (DiscoveryEnabledServer)server;
                //获取被选服务或标签
                String labelOr = discoveryEnabledServer.getInstanceInfo().getMetadata().get(LABEL_OR);
                if(StringUtils.isBlank(labelOr)){
                    //如果被选服务未设置或标签，则不选择这个被选服务
                    continue;
                }
                List<String> labelOrList = new ArrayList<>(Arrays.asList(labelOr.split(",")));

                //取交集
                labelOrList.retainAll(currLabelOrList);
                if(!labelOrList.isEmpty()){
                    //当交集不为空时，选中这个被选服务
                    result.add(server);
                }
            }
        }else if(StringUtils.isNotBlank(currLabelAnd)){
            //其次采用与标签
            List<String> currLabelAndList = new ArrayList<>(Arrays.asList(currLabelAnd.split(",")));
            for(Server server : reachableServers){
                DiscoveryEnabledServer discoveryEnabledServer = (DiscoveryEnabledServer)server;
                //获取被选服务与标签
                String labelAnd = discoveryEnabledServer.getInstanceInfo().getMetadata().get(LABEL_AND);
                if(StringUtils.isBlank(labelAnd)){
                    //如果被选服务未设置与标签，则不选择这个被选服务
                    continue;
                }
                List<String> labelAndList = new ArrayList<>(Arrays.asList(labelAnd.split(",")));

                //两集合完全相等才选中
                if(labelAndList.size() == currLabelAndList.size()) {
                    //集合中元素个数相等
                    labelAndList.removeAll(currLabelAndList);
                    if(labelAndList.isEmpty()){
                        //集合中各元素相等
                        result.add(server);
                    }
                }
            }
        }else{
            //都未设置则返回所有
            result = reachableServers;
        }
        return result;
    }

    /**
     * 过滤租户，<br>
     * 如果当前服务未设置租户，则他可以选择设置或未设置租户的所有服务，<br>
     * 如果当前服务设置了租户，则只选择与他设置了相同租户的服务，<br>
     * @param currServerInstanceInfo
     * @param reachableServers
     */
    private List<Server> tenantFilter(InstanceInfo currServerInstanceInfo, List<Server> reachableServers) {
        List<Server> result = new ArrayList<>();
        //获取当前服务的租户
        String currTenant = currServerInstanceInfo.getMetadata().get(TENANT);
        if(StringUtils.isNotBlank(currTenant)){
            //如果当前服务设置了租户，则只选择与他设置了相同租户的服务，
            for(Server server : reachableServers){
                DiscoveryEnabledServer discoveryEnabledServer = (DiscoveryEnabledServer)server;
                //获取被选服务租户
                String tenant = discoveryEnabledServer.getInstanceInfo().getMetadata().get(TENANT);
                if(currTenant.equalsIgnoreCase(tenant)){
                    result.add(server);
                }
            }
        }else{
            //如果当前服务未设置租户，则他可以选择设置或未设置所有服务，
            result = reachableServers;
        }
        return result;
    }

    @Override
    public Server choose(Object key) {
//        return choose(getLoadBalancer(), key);
        //这里调用自定义的选择服务方法
        return chooseByMilepost(getLoadBalancer(), key);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }
}
