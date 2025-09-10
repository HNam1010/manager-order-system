package com.example.order_service.dto.request;

import jakarta.validation.constraints.Email; // Import đúng
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    private String guestId; // Nullable - Frontend sẽ gửi nếu là khách

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên khách hàng quá dài")
    private String customerName;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 255, message = "Địa chỉ giao hàng quá dài")
    private String shippingAddress;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @Email(message = "Email không đúng định dạng") // Dùng jakarta validation
    @Size(max = 100, message = "Email quá dài")
    private String email; // Optional

    @Size(max = 500, message = "Ghi chú quá dài")
    private String orderNotes; // Optional

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // Ví dụ: "COD", "BANK_TRANSFER"
}
