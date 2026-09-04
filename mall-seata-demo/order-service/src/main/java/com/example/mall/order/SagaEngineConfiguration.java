package com.example.mall.order;

import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.engine.impl.DefaultStateMachineConfig;
import io.seata.saga.engine.impl.ProcessCtrlStateMachineEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaEngineConfiguration {
    public static final String STATE_MACHINE_RESOURCES =
            "classpath:statemachines/mall-order-saga.json";

    @Bean
    public DefaultStateMachineConfig stateMachineConfig() {
        DefaultStateMachineConfig config = new DefaultStateMachineConfig();
        config.setResources(new String[] {STATE_MACHINE_RESOURCES});
        config.setEnableAsync(false);
        return config;
    }

    @Bean
    public StateMachineEngine stateMachineEngine(DefaultStateMachineConfig config) {
        ProcessCtrlStateMachineEngine engine = new ProcessCtrlStateMachineEngine();
        engine.setStateMachineConfig(config);
        return engine;
    }
}
