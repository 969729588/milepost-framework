package com.milepost.admin.config.notifier;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.RemindingNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ruifu Hua on 2020/3/30.
 * 这里不需要实现租户隔离，因为我们在源头上实现了非当前租户下的实例的数据进不来。
 */
@Configuration
@ConditionalOnExpression("#{environment.getProperty('spring.mail.username')!=null && environment.getProperty('spring.mail.password')!=null && environment.getProperty('spring.boot.admin.notify.mail.to')!=null}")
public class NotifierConfig {

    private final InstanceRepository repository;
    private final ObjectProvider<List<Notifier>> otherNotifiers;

    public NotifierConfig(InstanceRepository repository, ObjectProvider<List<Notifier>> otherNotifiers) {
        this.repository = repository;
        this.otherNotifiers = otherNotifiers;
    }

    @Primary
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RemindingNotifier remindingNotifier() {
        RemindingNotifier notifier = new RemindingNotifier(filteringNotifier(), repository);
        notifier.setReminderPeriod(Duration.ofMinutes(1));//默认10m
        notifier.setCheckReminderInverval(Duration.ofSeconds(10));//默认10s
        return notifier;
    }

    /**
     * 配置这个bean后每个实例数据后面才能有一个“小铃铛”图标，可以抑制提醒通知的发送。
     * @return
     */
    @Bean
    public FilteringNotifier filteringNotifier() {
        CompositeNotifier delegate = new CompositeNotifier(otherNotifiers.getIfAvailable(Collections::emptyList));
        return new FilteringNotifier(delegate, repository);
    }
}
