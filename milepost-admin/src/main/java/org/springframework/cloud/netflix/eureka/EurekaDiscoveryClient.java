/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.netflix.eureka;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.milepost.core.multipleTenant.MultipleTenantProperties;
import com.milepost.core.spring.ApplicationContextProvider;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import static com.netflix.appinfo.InstanceInfo.PortType.SECURE;

/**
 * @author Spencer Gibb
 * @author Tim Ysewyn
 */
public class EurekaDiscoveryClient implements DiscoveryClient {

    public static final String DESCRIPTION = "Spring Cloud Eureka Discovery Client";

    private final EurekaClient eurekaClient;
    private final EurekaClientConfig clientConfig;

    //租户配置信息
    private MultipleTenantProperties multipleTenantProperties;

    @Deprecated
    public EurekaDiscoveryClient(EurekaInstanceConfig config, EurekaClient eurekaClient) {
        this(eurekaClient, eurekaClient.getEurekaClientConfig());
    }

    public EurekaDiscoveryClient(EurekaClient eurekaClient, EurekaClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.eurekaClient = eurekaClient;
        this.multipleTenantProperties = ApplicationContextProvider.getBean("multipleTenantProperties");
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        List<InstanceInfo> infos = this.eurekaClient.getInstancesByVipAddress(serviceId,
                false);
        List<ServiceInstance> instances = new ArrayList<>();
        for (InstanceInfo info : infos) {

            //过滤出当前租户下的实例，实现租户隔离
            if(!filterByTenant(info)){
                continue;
            }

            instances.add(new EurekaServiceInstance(info));
        }
        return instances;
    }

    /**
     * 过滤出当前租户下的实例，如果实例是当前租户下的，返回true
     * @param instanceInfo
     * @return
     */
    private boolean filterByTenant(InstanceInfo instanceInfo) {
        String turbineTenant = this.multipleTenantProperties.getTenant();
        String instanceTenant = instanceInfo.getMetadata().get("tenant");
        if(turbineTenant!=null && turbineTenant.equalsIgnoreCase(instanceTenant)){
            return true;
        }else{
            return false;
        }
    }

    public static class EurekaServiceInstance implements ServiceInstance {
        private InstanceInfo instance;

        public EurekaServiceInstance(InstanceInfo instance) {
            Assert.notNull(instance, "Service instance required");
            this.instance = instance;
        }

        public InstanceInfo getInstanceInfo() {
            return instance;
        }

        @Override
        public String getInstanceId() {
            return this.instance.getId();
        }

        @Override
        public String getServiceId() {
            return this.instance.getAppName();
        }

        @Override
        public String getHost() {
            return this.instance.getHostName();
        }

        @Override
        public int getPort() {
            if (isSecure()) {
                return this.instance.getSecurePort();
            }
            return this.instance.getPort();
        }

        @Override
        public boolean isSecure() {
            // assume if secure is enabled, that is the default
            return this.instance.isPortEnabled(SECURE);
        }

        @Override
        public URI getUri() {
            return DefaultServiceInstance.getUri(this);
        }

        @Override
        public Map<String, String> getMetadata() {
            return this.instance.getMetadata();
        }
    }

    @Override
    public List<String> getServices() {
        Applications applications = this.eurekaClient.getApplications();
        if (applications == null) {
            return Collections.emptyList();
        }
        List<Application> registered = applications.getRegisteredApplications();
        List<String> names = new ArrayList<>();
        for (Application app : registered) {
            if (app.getInstances().isEmpty()) {
                continue;
            }
            names.add(app.getName().toLowerCase());

        }
        return names;
    }

    @Override
    public int getOrder() {
        return clientConfig instanceof Ordered ? ((Ordered) clientConfig).getOrder() : DiscoveryClient.DEFAULT_ORDER;
    }
}
