package com.example.mall.order;

import io.seata.saga.statelang.domain.StateMachineInstance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final TccOrderService tcc;
    private final SagaOrderService saga;
    private final OrderStore store;

    public OrderController(TccOrderService tcc, SagaOrderService saga, OrderStore store) {
        this.tcc = tcc;
        this.saga = saga;
        this.store = store;
    }

    @PostMapping("/tcc")
    public OrderResult tcc(@RequestBody OrderRequest request) {
        tcc.place(request);
        return new OrderResult(request.getOrderId(), store.status(request.getOrderId()));
    }

    @PostMapping("/saga")
    public OrderResult saga(@RequestBody OrderRequest request) {
        StateMachineInstance instance = saga.place(request);
        return new OrderResult(request.getOrderId(), instance.getStatus().name());
    }

    @GetMapping("/{id}")
    public OrderResult get(@PathVariable String id) {
        return new OrderResult(id, store.status(id));
    }
}
