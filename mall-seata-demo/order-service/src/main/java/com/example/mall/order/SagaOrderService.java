package com.example.mall.order;

import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.statelang.domain.StateMachineInstance;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SagaOrderService {
    private final StateMachineEngine engine;

    public SagaOrderService(StateMachineEngine engine) {
        this.engine = engine;
    }

    public StateMachineInstance place(OrderRequest request) {
        Map<String, Object> input = new HashMap<>();
        input.put("request", request);
        return engine.startWithBusinessKey(
                "mallOrderSaga", null, request.getOrderId(), input);
    }
}
