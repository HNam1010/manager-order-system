package com.example.order_service.dto.request;

// DTO để gửi yêu cầu cập nhật kho hàng loạt
public class UpdateStockRequest {
    private Long productId;
    private Integer quantityToDecrease; // Số lượng cần giảm

    // Constructors, Getters, Setters
    public UpdateStockRequest() {}

    public UpdateStockRequest(Long productId, Integer quantityToDecrease) {
        this.productId = productId;
        this.quantityToDecrease = quantityToDecrease;
    }

    // Getters and Setters...
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantityToDecrease() { return quantityToDecrease; }
    public void setQuantityToDecrease(Integer quantityToDecrease) { this.quantityToDecrease = quantityToDecrease; }
}
