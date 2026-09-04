package com.example.mall.order;

public class OrderRequest {
    private String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private int points;
    private boolean failAfterOrder;

    public OrderRequest() { }

    public OrderRequest(String orderId, String productId, String userId, int quantity, int points) {
        this.orderId = orderId;
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
        this.points = points;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public boolean isFailAfterOrder() { return failAfterOrder; }
    public void setFailAfterOrder(boolean failAfterOrder) { this.failAfterOrder = failAfterOrder; }
}
