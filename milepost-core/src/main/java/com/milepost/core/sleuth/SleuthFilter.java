package com.milepost.core.sleuth;

import brave.Span;
import brave.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.instrument.web.TraceWebServletAutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by Ruifu Hua on 2020/4/8.
 */
@Component
//@ConditionalOnExpression("#{!'false'.equals(environment.getProperty('spring.rabbitmq.host'))}")
//存在track.enabled且值为true时加载这个bean，matchIfMissing=true表示不存在这个属性时也加载这个bean。
@ConditionalOnProperty(value = "track.enabled", havingValue = "true", matchIfMissing = true)
@Order(TraceWebServletAutoConfiguration.TRACING_FILTER_ORDER + 1)
public class SleuthFilter extends GenericFilterBean {

    private static Logger logger = LoggerFactory.getLogger(SleuthFilter.class);

    @Autowired(required = false)
    private Tracer tracer;

    @Autowired
    private Environment environment;

    public SleuthFilter() {
        logger.info("初始化SleuthFilter...");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Span currentSpan = tracer.currentSpan();

        String instanceId = environment.getProperty("eureka.instance.instance-id");
        String tenant = environment.getProperty("multiple-tenant.tenant");

        currentSpan.tag("instanceId", instanceId);
        currentSpan.tag("tenant", tenant);

        chain.doFilter(request, response);
    }
}
