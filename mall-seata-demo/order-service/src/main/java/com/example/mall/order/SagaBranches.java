package com.example.mall.order;

import org.springframework.stereotype.Component;

@Component("sagaBranches")
public class SagaBranches {
    private final BranchClient branches;
    private final OrderStore orders;

    public SagaBranches(BranchClient branches, OrderStore orders) {
        this.branches = branches;
        this.orders = orders;
    }

    public boolean deductStock(OrderRequest request) {
        return branches.product("saga/deduct", request.getOrderId(), request.getProductId(), request.getQuantity());
    }

    public boolean restoreStock(OrderRequest request) {
        return branches.restoreProduct(request.getOrderId());
    }

    public boolean deductPoints(OrderRequest request) {
        return branches.points("saga/deduct", request.getOrderId(), request.getUserId(), request.getPoints());
    }

    public boolean restorePoints(OrderRequest request) {
        return branches.restorePoints(request.getOrderId());
    }

    public boolean createOrder(OrderRequest request) {
        return orders.sagaCreate(request.getOrderId());
    }

    public boolean injectRequestedFailure(OrderRequest request) {
        if (request.isFailAfterOrder()) {
            throw new IllegalStateException("requested Saga compensation");
        }
        return true;
    }

    public boolean cancelOrder(OrderRequest request) {
        return orders.sagaCancel(request.getOrderId());
    }

    public boolean confirmOrder(OrderRequest request) {
        return orders.sagaConfirm(request.getOrderId());
    }
}
