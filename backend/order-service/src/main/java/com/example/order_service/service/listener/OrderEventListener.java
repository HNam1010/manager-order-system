package com.example.order_service.service.listener;

import com.example.order_service.service.serviceimpl.OrderConfirmedEvent;
import com.example.order_service.service.servicerepo.EmailService;
import jakarta.mail.MessagingException; // Thêm import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.io.UnsupportedEncodingException; // Thêm import

@Component
public class OrderEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    @Autowired private EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("Handling OrderConfirmedEvent for order code: {}", event.getOrder().getOrderCode());
        try {
            // Gọi EmailService để gửi mail xác nhận
            emailService.sendOrderConfirmationEmail(event.getOrder());
        } catch (MessagingException | UnsupportedEncodingException e) { // Bắt các exception cụ thể
            log.error("CRITICAL: Failed to send confirmation email after order {} was confirmed. Error: {}",
                    event.getOrder().getOrderCode(), e.getMessage(), e);
        } catch (Exception e) { // Bắt các lỗi khác
            log.error("CRITICAL: Unexpected error sending confirmation email for order {}: {}",
                    event.getOrder().getOrderCode(), e.getMessage(), e);
        }
    }
}
