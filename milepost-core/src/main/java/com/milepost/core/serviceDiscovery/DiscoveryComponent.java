package com.milepost.core.serviceDiscovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/4/3.
 */
@Component
public class DiscoveryComponent {

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * 获取所有租户
     * @return
     */
    public List<String> getAllTenant(){
        List<String> result = new ArrayList<>();

        List<String> services = discoveryClient.getServices();
        for(String service : services){
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            for(ServiceInstance serviceInstance : instances){
                Map<String, String> metadata = serviceInstance.getMetadata();
                String tenant = metadata.get("tenant");
                if(!result.contains(tenant)){
                    result.add(tenant);
                }
            }
        }
        return result;
    }

    /**
     * 获取指定租户下的所有服务(小写形式)
     * @param tenant
     * @return
     */
    public List<String> getAllServiceNameByTenant(String tenant){
        List<String> result = new ArrayList<>();

        List<String> services = discoveryClient.getServices();
        for(String service : services){
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            for(ServiceInstance serviceInstance : instances){
                Map<String, String> metadata = serviceInstance.getMetadata();
                String tenantCurr = metadata.get("tenant");
                if(tenant.equalsIgnoreCase(tenantCurr)){
                    result.add(service);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获取指定租户下的，指定服务下的所有实例ID
     * @param serviceName
     * @param tenant
     * @return
     */
    public List<String> getAllInstanceIdsByServiceAndTenant(String serviceName, String tenant){
        List<String> result = new ArrayList<>();

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        for(ServiceInstance serviceInstance : instances){
            String instanceId = serviceInstance.getInstanceId();
            Map<String, String> metadata = serviceInstance.getMetadata();
            String tenantCurr = metadata.get("tenant");
            if(tenant.equalsIgnoreCase(tenantCurr)){
                result.add(instanceId);
            }
        }
        return result;
    }
}
