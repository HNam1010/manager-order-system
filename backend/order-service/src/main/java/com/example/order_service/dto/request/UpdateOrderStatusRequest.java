package com.example.order_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "ID trạng thái mới không được để trống")
    private Integer newStatusId; // Khớp với kiểu khóa chính của OrderStatus
}