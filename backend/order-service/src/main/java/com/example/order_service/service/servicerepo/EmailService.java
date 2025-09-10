package com.example.order_service.service.servicerepo;

import com.example.order_service.entity.Order;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailService {

     // Gửi email xác nhận đơn hàng đã được admin duyệt.
     // @param order Đối tượng Order chứa đầy đủ thông tin (bao gồm cả OrderDetails).
    void sendOrderConfirmationEmail(Order order) throws MessagingException, UnsupportedEncodingException;

}
