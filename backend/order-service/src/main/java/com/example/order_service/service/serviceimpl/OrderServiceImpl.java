package com.example.order_service.service.serviceimpl;

import com.example.be.commons.ApiResponse;
import com.example.be.commons.handler.exception.*;
import com.example.be.commons.handler.exception.ResourceNotFoundException;
import com.example.order_service.client.CartServiceClient;
import com.example.order_service.client.ProductServiceClient;
import com.example.order_service.dto.client.CartItemData;
import com.example.order_service.dto.client.ProductData;
import com.example.order_service.dto.request.PlaceOrderRequest;
import com.example.order_service.dto.reponse.OrderResponse;
import com.example.order_service.dto.request.UpdateStockRequest;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderDetail;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.repository.OrderDetailRepository;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.OrderStatusRepository;
import com.example.order_service.service.servicerepo.OrderService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Để tạo order code

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private OrderStatusRepository orderStatusRepository;
    @Autowired private CartServiceClient cartServiceClient;
    @Autowired private ProductServiceClient productServiceClient;
    @Autowired private OrderMapper orderMapper;

    //gửi mail
    @Autowired private ApplicationEventPublisher applicationEventPublisher;
    private static final String STATUS_CONFIRMED_CODE = "CONFIRMED";
    private static final Integer CONFIRMED_STATUS_ID = 2;

    private static final String INITIAL_ORDER_STATUS_CODE = "PENDING_CONFIRMATION"; //  mã trạng thái ban đầu

    @Override
    @Transactional // Đảm bảo tất cả thao tác DB trong service này là một transaction
    public OrderResponse placeOrder(Long userId, String guestId, PlaceOrderRequest request) {
        log.info("Placing order for userId: {}, guestId: {}", userId, guestId);

        // 1. Lấy giỏ hàng từ Cart Service
        ApiResponse<List<CartItemData>> cartResponse;
        try {
            log.debug("Calling Cart Service to get items...");
            cartResponse = cartServiceClient.getCartItems(userId, guestId);
        } catch (Exception e) {
            log.error("Error calling Cart Service", e);
            throw new ServiceUnavailableException("Không thể kết nối đến dịch vụ giỏ hàng.", e);
        }

        if (cartResponse == null || !cartResponse.isSuccess() || cartResponse.getData() == null || cartResponse.getData().isEmpty()) {
            log.warn("Cart is empty or failed to fetch from Cart Service. Message: {}", cartResponse != null ? cartResponse.getMessage() : "N/A");
            throw new EmptyCartException("Giỏ hàng trống hoặc không thể lấy thông tin giỏ hàng.");
        }
        List<CartItemData> cartItems = cartResponse.getData();
        log.info("Received {} items from cart service.", cartItems.size());


        // 2. Kiểm tra tồn kho và lấy thông tin sản phẩm snapshot
        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal calculatedTotalAmount = BigDecimal.ZERO;

        for (CartItemData cartItem : cartItems) {
            ProductData productData = fetchAndValidateProduct(cartItem.getProductId(), cartItem.getQuantity());

            OrderDetail detail = new OrderDetail();
            detail.setProductId(productData.getSerialId());
            detail.setProductName(productData.getName());
            detail.setProductBrand(productData.getBrand());
            detail.setProductPrice(productData.getPrice()); // Giá lúc kiểm tra
            detail.setProductImagePath(productData.getImagePath()); // Lưu path ảnh
            detail.setQuantity(cartItem.getQuantity());
            detail.setTotalPrice(productData.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            orderDetails.add(detail);
            calculatedTotalAmount = calculatedTotalAmount.add(detail.getTotalPrice());
            log.debug("Added product {} to order details. Subtotal: {}", productData.getName(), detail.getTotalPrice());
        }
        log.info("Calculated total amount: {}", calculatedTotalAmount);

        // 3. Lấy trạng thái ban đầu
        OrderStatus initialStatus = orderStatusRepository.findByStatusCode(INITIAL_ORDER_STATUS_CODE)
                .orElseThrow(() -> new OrderProcessingException("Không tìm thấy trạng thái đơn hàng ban đầu: " + INITIAL_ORDER_STATUS_CODE));


        // 4. Tạo đối tượng Order
        Order order = new Order();
        order.setUserId(userId); // Có thể null nếu là guest
        order.setCustomerName(request.getCustomerName());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setShippingAddress(request.getShippingAddress());
        order.setEmail(request.getEmail());
        order.setOrderNotes(request.getOrderNotes());
        order.setTotalAmount(calculatedTotalAmount);
        order.setStatus(initialStatus);

        //set paymentMethod (String) TRỰC TIẾP TỪ REQUEST
        order.setPaymentMethod(request.getPaymentMethod());
        order.setOrderCode(generateOrderCode()); // Tạo mã đơn hàng duy nhất


        // *** TẠO VÀ LƯU GUEST TOKEN NẾU LÀ KHÁCH
        if (userId == null) {
            String token = UUID.randomUUID().toString();
            order.setGuestToken(token);
            //Đặt thời gian hết hạn cho token 1 ngày sau
            order.setGuestTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
            log.info("Generated guest token for guest order: {}", token);
        }

        // Thêm các OrderDetail vào Order (thiết lập quan hệ hai chiều)
        orderDetails.forEach(order::addOrderDetail);

        // 5. Lưu Order và OrderDetails (Nhờ CascadeType.ALL)
        Order savedOrder = orderRepository.save(order);
        log.info("Order saved successfully with ID: {} and Order Code: {}", savedOrder.getSerialId(), savedOrder.getOrderCode());

        // 6. Xử lý sau khi lưu thành công
        // Publish sự kiện để Product Service trừ tồn kho
        publishOrderPlacedEvent(savedOrder); // Implement hàm này nếu dùng message queue

        // Gọi API xóa giỏ hàng
        clearCartAfterOrder(userId, guestId);


        // 7. Map sang DTO và trả về
        return orderMapper.toOrderResponse(savedOrder);
    }


    // Helper để gọi Product Service và kiểm tra tồn kho
    private ProductData fetchAndValidateProduct(Long productId, Integer requestedQuantity) {
        ApiResponse<ProductData> productResponse;
        try {
            log.debug("Fetching product details for stock check - productId: {}", productId);
            productResponse = productServiceClient.getProductById(productId);
        } catch (Exception e) {
            log.error("Error calling Product Service for stock check - productId: {}", productId, e);
            throw new ServiceUnavailableException("Không thể kết nối đến dịch vụ sản phẩm để kiểm tra tồn kho.", e);
        }

        if (productResponse == null || !productResponse.isSuccess() || productResponse.getData() == null) {
            log.warn("Product not found or fetch failed from Product Service for productId: {}", productId);
            throw new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + productId);
        }

        ProductData productData = productResponse.getData();
        if (productData.getQuantity() == null || productData.getQuantity() < requestedQuantity) {
            log.warn("Product out of stock. ProductId: {}, Requested: {}, Available: {}",
                    productId, requestedQuantity, productData.getQuantity());
            throw new OutOfStockException("Sản phẩm '" + productData.getName() + "' không đủ số lượng tồn kho (Còn lại: " + productData.getQuantity() + ").");
        }
        log.debug("Stock check passed for productId: {}. Requested: {}, Available: {}", productId, requestedQuantity, productData.getQuantity());
        return productData;
    }

    // Helper để publish sự kiện với RabbitMQ
    private void publishOrderPlacedEvent(Order order) {

        log.warn("Event publishing (e.g., RabbitMQ) for stock update is not implemented yet."); // Placeholder
    }

    // Helper để gọi API xóa giỏ hàng
    private void clearCartAfterOrder(Long userId, String guestId) {
        try {
            log.info("Calling Cart Service to clear cart for userId: {}, guestId: {}", userId, guestId);
            ApiResponse<Void> clearResponse = cartServiceClient.clearCart(userId, guestId);
            if (clearResponse == null || !clearResponse.isSuccess()) {
                log.warn("Failed to clear cart after order placement. Message: {}", clearResponse != null ? clearResponse.getMessage() : "N/A");
                // Ghi log lỗi nhưng không nên làm hỏng transaction đặt hàng chỉ vì không xóa được giỏ hàng
            } else {
                log.info("Cart cleared successfully after order placement.");
            }
        } catch (Exception e) {
            log.error("Error calling Cart Service to clear cart after order placement", e);
        }
    }

    // Helper tạo mã đơn hàng
    private String generateOrderCode() {
        // Ví dụ đơn giản: OR + timestamp + UUID ngắn
        return "OR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }


    // --- Implement các phương thức khác của OrderService ---
    @Override
    public OrderResponse getOrderById(Long orderId) {
        // Nên fetch cả details và status
        Order order = orderRepository.findByIdWithDetailsAndStatus(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderByIdAndUser(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserIdWithDetailsAndStatus(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId + " cho người dùng này."));
        return orderMapper.toOrderResponse(order);
    }


    @Override
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching orders for userId: {}", userId);
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        return orderPage.map(orderMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable, Integer statusId) {
        log.debug("Fetching all orders for admin. Pageable: {}, StatusId: {}", pageable, statusId);
        Page<Order> orderPage;

        if (statusId != null) {

            log.info("Filtering orders by statusId: {}", statusId);

            orderPage = orderRepository.findByStatus_SerialId(statusId, pageable);
        } else {
            // Nếu không có statusId, lấy tất cả (như cũ)
            log.info("Fetching all orders without status filter.");
            orderPage = orderRepository.findAllWithStatus(pageable);
        }

        return orderPage.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Integer newStatusId) {
        log.info("Updating status for orderId: {} to newStatusId: {}", orderId, newStatusId);

        // LUÔN FETCH ĐỦ CHI TIẾT Ở ĐÂY ĐỂ CÓ OrderDetails
        Order order = orderRepository.findByIdWithDetailsAndStatus(orderId) // Sử dụng phương thức đã có
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Lấy trạng thái mới từ DB
        OrderStatus newStatus = orderStatusRepository.findById(newStatusId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái đơn hàng với ID: " + newStatusId));

        // KIỂM TRA: Chỉ cập nhật kho khi chuyển sang trạng thái CONFIRMED trạng thái hiện tại *chưa phải* là CONFIRMED/COMPLETED/CANCELLED
        boolean shouldUpdateStock = newStatusId.equals(CONFIRMED_STATUS_ID) // ID của trạng thái CONFIRMED
                && order.getStatus().getSerialId() != CONFIRMED_STATUS_ID // Tránh trừ kho nhiều lần
                && order.getStatus().getStatusCode().equals(INITIAL_ORDER_STATUS_CODE); //  từ PENDING_CONFIRMATION

        if (shouldUpdateStock) {
            log.info("Order {} is being confirmed. Proceeding to update product stock.", orderId);
            try {
                updateProductStockForOrder(order); // Gọi hàm xử lý cập nhật kho
            } catch (FeignException e) {
                // Lỗi giao tiếp với Product Service
                log.error("Failed to update product stock due to communication error with Product Service for order {}. Status: {}, Body: {}", orderId, e.status(), e.contentUTF8(), e);
                // xóa sản phẩm khi admin xác nhận đơn hàng từ đây
                // 1. Throw Exception và Rollback transaction: An toàn nhất, không xác nhận đơn nếu không trừ được kho.
                throw new ServiceUnavailableException("Lỗi khi cập nhật tồn kho sản phẩm. Không thể xác nhận đơn hàng.", e);

               // log.error("Stock update failed but proceeding with order status update...");
            } catch (ProductUpdateException e) {
                log.error("Failed to update product stock due to business error from Product Service for order {}: {}", orderId, e.getMessage(), e);
                throw new OrderProcessingException("Lỗi nghiệp vụ khi cập nhật tồn kho: " + e.getMessage(), e); //trả về Rollback
            } catch (Exception e) {
                log.error("Unexpected error during product stock update for order {}", orderId, e);
                throw new OrderProcessingException("Lỗi không xác định khi cập nhật tồn kho.", e); // Rollback
            }
        } else {
            log.info("Skipping stock update for order {} (Not a confirmation transition or already processed). New status ID: {}", orderId, newStatusId);
        }


        // Phần gửi mail
        boolean shouldPublishEmailEvent = STATUS_CONFIRMED_CODE.equalsIgnoreCase(newStatus.getStatusCode()) // Hoặc dùng newStatusId == CONFIRMED_STATUS_ID
                && order.getEmail() != null && !order.getEmail().isBlank();

        // Cập nhật trạng thái đơn hàng (luôn thực hiện sau khi kiểm tra/cập nhật kho)
        order.setStatus(newStatus);
        orderRepository.save(order); // Lưu trạng thái mới của đơn hàng
        log.info("Order status updated successfully for orderId: {}. New status: {}", orderId, newStatus.getStatusCode());


        if (shouldPublishEmailEvent) {
            applicationEventPublisher.publishEvent(new OrderConfirmedEvent(this, order));
            log.info("Published OrderConfirmedEvent for order code: {}", order.getOrderCode());
        }


        // Trả về response đã được map từ 'order' đã fetch
        // Lưu ý: Dữ liệu Product trong OrderDetails là snapshot lúc đặt hàng, không phải số lượng mới nhất.
        // Nếu cần trả về số lượng mới nhất, cần gọi lại Product Service.
        return orderMapper.toOrderResponse(order);
    }


    //THÊM HÀM MỚI CHO KHÁCH XEM ĐƠN HÀNG BẰNG TOKEN
    public OrderResponse getGuestOrderByIdAndToken(Long orderId, String guestToken) {
        if (guestToken == null || guestToken.isBlank()) {
            throw new InvalidRequestException("Yêu cầu thiếu mã truy cập đơn hàng.");
        }

        Order order = orderRepository.findByIdWithDetailsAndStatus(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra xem đây có phải đơn hàng của khách không (userId == null) và token có khớp không
        if (order.getUserId() != null || !guestToken.equals(order.getGuestToken())) {
            log.warn("Invalid guest token access attempt for order {}. Provided token: {}", orderId, guestToken);
            throw new ForbiddenAccessException("Mã truy cập không hợp lệ hoặc đơn hàng không tồn tại.");
        }

         //Optional: Kiểm tra token hết hạn
         if (order.getGuestTokenExpiresAt() != null && Instant.now().isAfter(order.getGuestTokenExpiresAt())) {
             throw new ForbiddenAccessException("Mã truy cập đơn hàng đã hết hạn.");
         }


        log.info("Guest access granted for order {} using token.", orderId);
        return orderMapper.toOrderResponse(order);
    }

    // Hàm helper để cập nhật kho (gọi Product Service)
    private void updateProductStockForOrder(Order order) {
        List<UpdateStockRequest> stockUpdates = new ArrayList<>();
        for (OrderDetail detail : order.getOrderDetails()) {
            // Tạo request giảm số lượng cho từng sản phẩm
            stockUpdates.add(new UpdateStockRequest(detail.getProductId(), detail.getQuantity()));
            log.debug("Prepared stock update for Product ID: {}, Decrease Quantity: {}", detail.getProductId(), detail.getQuantity());
        }

        if (!stockUpdates.isEmpty()) {
            log.info("Calling Product Service to decrease stock for {} items in order {}", stockUpdates.size(), order.getSerialId());
            // Gọi API mới trong ProductServiceClient (ví dụ: decreaseStock)
            ApiResponse<Void> stockUpdateResponse = productServiceClient.decreaseStock(stockUpdates); // API này cần được tạo

            // Kiểm tra kết quả từ Product Service
            if (stockUpdateResponse == null || !stockUpdateResponse.isSuccess()) {
                String errorMessage = stockUpdateResponse != null ? stockUpdateResponse.getMessage() : "Phản hồi không hợp lệ từ Product Service.";
                log.error("Product Service failed to decrease stock for order {}. Message: {}", order.getSerialId(), errorMessage);
                // Ném một Exception cụ thể để transaction có thể rollback
                throw new ProductUpdateException("Product Service không thể cập nhật tồn kho: " + errorMessage);
            }
            log.info("Product Service successfully processed stock decrease for order {}", order.getSerialId());
        } else {
            log.warn("No order details found to update stock for order {}", order.getSerialId());
        }

    }
}
