package com.example.mall.points;

public class BranchRequest {
    private String orderId;
    private String resourceId;
    private int amount;

    public BranchRequest() { }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
}
