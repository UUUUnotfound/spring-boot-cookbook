package com.tangcheng.learning.eventbus;

import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by tang.cheng on 2016/8/17.
 */
@Configuration
@ComponentScan("com.tangcheng.learning.eventbus")
public class EventBusConfig {

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }
}
