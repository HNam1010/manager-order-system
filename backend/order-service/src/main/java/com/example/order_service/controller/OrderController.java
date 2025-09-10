package com.example.order_service.controller;

import com.example.be.commons.ApiResponse;
import com.example.be.commons.handler.exception.ForbiddenAccessException;
import com.example.be.commons.handler.exception.ResourceNotFoundException;
import com.example.order_service.dto.request.PlaceOrderRequest;
import com.example.order_service.dto.request.UpdateOrderStatusRequest;
import com.example.order_service.dto.reponse.OrderResponse;
import com.example.be.commons.handler.exception.InvalidRequestException;
import com.example.order_service.service.servicerepo.OrderService;
import jakarta.servlet.http.HttpServletRequest; // Import để lấy Guest ID
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Import các annotation

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    private Long parseUserIdHeader(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) return null;
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            log.warn("Invalid X-User-ID format: {}", userIdHeader);
            return null;
        }
    }

    // Đặt hàng
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request, // Request đã chứa guestId nếu là khách
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader) { // Chỉ cần userIdHeader

        Long userId = parseUserIdHeader(userIdHeader);
        // Lấy guestId trực tiếp từ request body nếu là khách
        String guestId = (userId == null) ? request.getGuestId() : null;
       // String guestId = null; // Service sẽ tự xử lý guestId nếu userId null

        log.info("API POST /orders - userId: {}, guestId from RequestBody: {}", userId, guestId);

        // Kiểm tra thông tin cần thiết
        if (userId == null && guestId == null) {
            // Trường hợp này không nên xảy ra nếu frontend gửi đúng guestId khi chưa đăng nhập
            log.error("Order request is missing both userId and guestId.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Không thể xác định người dùng hoặc khách."));
        }

        // Nếu là khách (userId null), cần thông tin khách và guestId trong body
        if (userId == null && (
                request.getCustomerName() == null || request.getCustomerName().isBlank() ||
                        request.getEmail() == null || request.getEmail().isBlank() || // Email nên bắt buộc cho khách để liên lạc
                        request.getPhoneNumber() == null || request.getPhoneNumber().isBlank() ||
                        request.getShippingAddress() == null || request.getShippingAddress().isBlank() ||
                        guestId == null || guestId.isBlank() // Kiểm tra guestId từ request body
        )) {
            log.warn("Guest checkout attempt without required customer information OR guestId in the request body.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Vui lòng cung cấp đầy đủ thông tin khách hàng và đảm bảo giỏ hàng hợp lệ."));
        }

        // Gọi service với userId (Long) và guestId (String) đã lấy được
        OrderResponse createdOrder = orderService.placeOrder(userId, guestId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt hàng thành công.", createdOrder));
    }

    // Lấy lịch sử đơn hàng người dùng
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader,
            HttpServletRequest request,
            @PageableDefault(page = 0, size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = parseUserIdHeader(userIdHeader);
        if (userId == null) {
            log.warn("GET /my-orders missing or invalid X-User-ID");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Yêu cầu đăng nhập để xem đơn hàng."));
        }

        log.info("API GET /my-orders - userId: {}", userId);
        Page<OrderResponse> orderPage = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử đơn hàng thành công.", orderPage));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader,
            @RequestHeader(name = "X-User-Roles", required = false) List<String> roles,
            //NHẬN TOKEN TỪ QUERY PARAM
            @RequestParam(name = "token", required = false) String guestToken) {

        Long userId = parseUserIdHeader(userIdHeader);
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");

        log.info("API GET /orders/{} - userId: {}, isAdmin: {}, guestToken: {}", orderId, userId, isAdmin, guestToken);

        OrderResponse orderResponse;
        try {
            if (isAdmin) {
                log.debug("Admin access granted for order {}", orderId);
                orderResponse = orderService.getOrderById(orderId); // Admin xem không cần token khách
            } else if (userId != null) {
                log.debug("Authenticated user access for order {}. Verifying ownership for userId {}", orderId, userId);
                orderResponse = orderService.getOrderByIdAndUser(orderId, userId);
            } else if (guestToken != null) {
                //GỌI HÀM MỚI CHO KHÁCH
                log.debug("Guest access attempt for order {} using token.", orderId);
                orderResponse = orderService.getGuestOrderByIdAndToken(orderId, guestToken);
            }
            else {
                // Không có userId và không có guestToken
                log.warn("Unauthorized attempt to access order {}", orderId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Yêu cầu đăng nhập hoặc cung cấp mã truy cập đơn hàng hợp lệ."));
            }
            return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công.", orderResponse));

        } catch (ResourceNotFoundException e) {
            log.warn("Order {} not found.", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (ForbiddenAccessException e) { // Bắt lỗi token không hợp lệ hoặc không có quyền
            log.warn("Access denied for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (InvalidRequestException e) { // Bắt lỗi thiếu token
            log.warn("Invalid request for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching order details for orderId: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi máy chủ khi lấy chi tiết đơn hàng."));
        }
    }

    // Admin: Lấy tất cả đơn hàng
    @GetMapping("/admin/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrdersForAdmin(
            @RequestHeader(name = "X-User-Roles", required = true) List<String> roles,
            @PageableDefault(page = 0, size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
            //lọc theo id trạng thái
            @RequestParam(name = "statusId", required = false) Integer statusId

    ) {

        log.info("API GET /admin/orders - roles: {}", roles);
        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Yêu cầu quyền Admin."));
        }

        Page<OrderResponse> orderPage = orderService.getAllOrders(pageable, statusId);
        return ResponseEntity.ok(ApiResponse.success("Lấy tất cả đơn hàng thành công.", orderPage));
    }

    //Cập nhật trạng thái đơn hàng
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestHeader(name = "X-User-Roles", required = true) List<String> roles) {

        log.info("API PUT /orders/{}/status - roles: {}", orderId, roles);

        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Yêu cầu quyền Admin."));
        }

        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, request.getNewStatusId());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công.", updatedOrder));
    }



}
