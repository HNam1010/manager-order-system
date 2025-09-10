package com.example.product_service.dto.request;

// DTO để gửi yêu cầu cập nhật kho từ các service khác
public class UpdateStockRequest {
    private Long productId;
    private Integer quantityToDecrease; // Số lượng cần giảm

    // Constructors (ít nhất là một constructor mặc định cho Jackson/Feign)
    public UpdateStockRequest() {}

    public UpdateStockRequest(Long productId, Integer quantityToDecrease) {
        this.productId = productId;
        this.quantityToDecrease = quantityToDecrease;
    }

    // Getters và Setters (BẮT BUỘC cho Jackson/Feign hoạt động đúng)
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantityToDecrease() {
        return quantityToDecrease;
    }

    public void setQuantityToDecrease(Integer quantityToDecrease) {
        this.quantityToDecrease = quantityToDecrease;
    }

    // (Optional) toString() để dễ debug
    @Override
    public String toString() {
        return "UpdateStockRequest{" +
                "productId=" + productId +
                ", quantityToDecrease=" + quantityToDecrease +
                '}';
    }
}