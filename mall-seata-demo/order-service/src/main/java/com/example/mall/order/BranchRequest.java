package com.example.mall.order;

public class BranchRequest {
    private String orderId;
    private String resourceId;
    private int amount;

    public BranchRequest() { }
    public BranchRequest(String orderId, String resourceId, int amount) {
        this.orderId = orderId;
        this.resourceId = resourceId;
        this.amount = amount;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
}
