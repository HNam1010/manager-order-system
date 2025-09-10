package com.example.order_service.service.serviceimpl;

import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderDetail; // Import OrderDetail
import com.example.order_service.service.servicerepo.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale; // Import Locale

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired private JavaMailSender mailSender;

    @Value("${app.mail.from}") private String fromEmail;
    @Value("${app.mail.sender-name}") private String senderName;

    // Định dạng ngày giờ Việt Nam
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh")); // Đặt múi giờ Việt Nam

    @Override
    @Async // Chạy bất đồng bộ
    public void sendOrderConfirmationEmail(Order order) throws MessagingException, UnsupportedEncodingException {
        if (order.getEmail() == null || order.getEmail().isBlank()) {
            log.warn("Cannot send confirmation email for order code {} because email is missing.", order.getOrderCode());
            return;
        }

        log.info("Attempting to send confirmation email for order code: {}", order.getOrderCode());
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // true = multipart message, true = html, "UTF-8" = encoding
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromEmail, senderName);
        helper.setTo(order.getEmail());
        helper.setSubject("Đơn hàng #" + order.getOrderCode() + " đã được xác nhận");

        // Xây dựng bảng chi tiết sản phẩm HTML
        StringBuilder itemsHtml = new StringBuilder();
        itemsHtml.append("<table border='1' cellpadding='7' cellspacing='0' style='border-collapse: collapse; width: 100%; font-size: 14px; border-color: #ddd;'>");
        itemsHtml.append("<thead style='background-color: #f8f9fa;'><tr><th style='text-align:left;'>Sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th style='text-align:right;'>Thành tiền</th></tr></thead><tbody>");

        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            for (OrderDetail detail : order.getOrderDetails()) {
                itemsHtml.append("<tr>");
                itemsHtml.append("<td style='text-align:left;'>").append(detail.getProductName())
                        .append(detail.getProductBrand() != null ? "<br/><small style='color:#6c757d;'>Thương hiệu: " + detail.getProductBrand() + "</small>" : "")
                        .append("</td>");
                itemsHtml.append("<td style='text-align:center;'>").append(detail.getQuantity()).append("</td>");
                // Định dạng tiền tệ Việt Nam
                itemsHtml.append("<td style='text-align:right;'>").append(String.format(Locale.GERMAN, "%,.0f đ", detail.getProductPrice())).append("</td>");
                itemsHtml.append("<td style='text-align:right;'>").append(String.format(Locale.GERMAN, "%,.0f đ", detail.getTotalPrice())).append("</td>");
                itemsHtml.append("</tr>");
            }
        } else {
            itemsHtml.append("<tr><td colspan='4'>Không có thông tin chi tiết sản phẩm.</td></tr>");
        }
        itemsHtml.append("</tbody></table>");

        // Xây dựng nội dung HTML chính
        String htmlContent = String.format(
                """
                <html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
                    <div style='max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #eee; border-radius: 5px;'>
                        <h2 style='color: #0d6efd;'>Chào %s,</h2>
                        <p>Chúng tôi vui mừng vì bạn đã tin tưởng và đặt hàng của chúng tôi, chúng tôi xin thông báo đơn hàng <strong>#%s</strong> của bạn đặt vào lúc %s đã được <strong>xác nhận</strong>.</p>
                        <p>Đơn hàng đang được chuẩn bị và dự kiến trong 3 ngày sẽ giao đến địa chỉ:</p>
                        <p style='background-color: #f8f9fa; padding: 10px; border-radius: 4px;'><i>%s</i></p>
                        <p>Số điện thoại liên hệ: %s</p>
                        <p>Phương thức thanh toán đã chọn: %s</p>
                        <hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>
                        <h3 style='color: #0d6efd;'>Chi tiết đơn hàng:</h3>
                        %s
                        <p style='text-align:right; font-weight:bold; font-size: 1.2em; margin-top: 15px;'>Tổng cộng: <span style='color: #dc3545;'>%s đ</span></p>
                        <p>Cảm ơn bạn đã tin tưởng và mua sắm tại cửa hàng của chúng tôi!</p>
                        <p>Trân trọng,<br/><strong>Đội ngũ %s</strong></p>
                    </div>
                </body></html>
                """,
                order.getCustomerName(),
                order.getOrderCode(),
                DATE_TIME_FORMATTER.format(order.getOrderDate()), // Sử dụng formatter
                order.getShippingAddress(),
                order.getPhoneNumber(),
                // Hiển thị tên phương thức thanh toán dễ hiểu hơn
                "BANK_TRANSFER".equalsIgnoreCase(order.getPaymentMethod()) ? "Chuyển khoản ngân hàng" : "Khi nhận hàng (COD)",
                itemsHtml.toString(),
                String.format(Locale.GERMAN, "%,.0f", order.getTotalAmount()), // Định dạng tổng tiền
                senderName // Tên cửa hàng
        );

        helper.setText(htmlContent, true); // true = HTML
        mailSender.send(mimeMessage);
        log.info("Order confirmation email sent successfully to {} for order code: {}", order.getEmail(), order.getOrderCode());
    }
}