package com.example.cart_service.controller;

import com.example.be.commons.ApiResponse;
import com.example.cart_service.dto.reponse.CartItemDTO;
import com.example.cart_service.dto.request.AddToCartRequest;
import com.example.cart_service.dto.request.UpdateCartItemRequest;
import com.example.cart_service.service.servicerepo.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    private String getGuestId(HttpServletRequest request) {
        String guestId = request.getHeader("X-Guest-ID");
        log.debug("Extracted Guest ID from header: {}", guestId);
        return (guestId != null && !guestId.isBlank()) ? guestId : null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCart(
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader, // Nhận header dạng String
            HttpServletRequest request) {
        Long userId = parseUserIdHeader(userIdHeader); // Chuyển đổi an toàn về long
        String guestId = (userId == null) ? getGuestId(request) : null;
        log.info("API GET /cart - userId: {}, guestId: {}", userId, guestId);

        if (userId == null && guestId == null) {
            log.warn("GET /cart request without userId or guestId");
            return ResponseEntity.ok(ApiResponse.success("Giỏ hàng trống (chưa xác định)", List.of()));
        }

        List<CartItemDTO> cartItems = cartService.getCart(userId, guestId);
        return ResponseEntity.ok(ApiResponse.success("Lấy giỏ hàng thành công", cartItems));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCartItemCount(
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader, // Nhận header dạng String
            HttpServletRequest request) {
        Long userId = parseUserIdHeader(userIdHeader);
        String guestId = (userId == null) ? getGuestId(request) : null;
        log.info("API GET /cart/count - userId: {}, guestId: {}", userId, guestId);

        if (userId == null && guestId == null) {
            return ResponseEntity.ok(ApiResponse.success("Số lượng: 0", 0L));
        }

        long count = cartService.getCartItemCount(userId, guestId);
        return ResponseEntity.ok(ApiResponse.success("Lấy số lượng thành công", count));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<CartItemDTO>> addItem(
            @Valid @RequestBody AddToCartRequest addItemRequest,
            @RequestHeader(name = "X-User-ID", required = false) String userId,
            HttpServletRequest request) {
        String guestId = (userId == null) ? getGuestId(request) : null;
        Long longUserId = null; // Khai báo Long userId

        try {
            if (userId != null && !userId.isEmpty()) {
                longUserId = Long.parseLong(userId); // Chuyển đổi String sang Long
            }

        } catch (NumberFormatException e) {
            // Xử lý lỗi nếu userId không hợp lệ (không phải là số)
            log.warn("Invalid X-User-ID format: {}. Ignoring user ID.", userId);
        }

        log.info("API POST /cart - userId: {}, guestId: {}", longUserId, guestId);

        if (longUserId == null && guestId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Không thể thêm vào giỏ hàng khi chưa xác định người dùng hoặc khách."));
        }

        CartItemDTO addedItem = cartService.addItemToCart(longUserId, guestId, addItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Thêm vào giỏ hàng thành công", addedItem));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateItemQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest updateRequest,
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader,
            HttpServletRequest request) {
        Long userId = parseUserIdHeader(userIdHeader); // Chuyển đổi an toàn
        String guestId = (userId == null) ? getGuestId(request) : null;
        log.info("API PUT /cart/{} - userId: {}, guestId: {}", cartItemId, userId, guestId);

        if (userId == null && guestId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Không thể cập nhật giỏ hàng khi chưa xác định người dùng hoặc khách."));
        }

        CartItemDTO updatedItem = cartService.updateItemQuantity(userId, guestId, cartItemId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật số lượng thành công", updatedItem));
    }

    @DeleteMapping // Mapping với phương thức DELETE
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader,
            @RequestHeader(name = "X-Guest-ID", required = false) String guestIdHeader // Nhận cả guestId từ header nếu được gửi
    ) {

        Long userId = parseUserIdHeader(userIdHeader);
        // Ưu tiên guestId từ header nếu có, phòng trường hợp cần xóa giỏ khách từ nơi khác
        String guestId = (userId == null) ? (guestIdHeader != null ? guestIdHeader : null) : null;

        log.info("API DELETE /cart - Clearing cart for userId: {}, guestId: {}", userId, guestId);

        if (userId == null && guestId == null) {
            // Trả về lỗi nếu không xác định được giỏ hàng cần xóa
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Không thể xóa giỏ hàng khi chưa xác định người dùng hoặc khách."));
        }

        cartService.clearCart(userId, guestId);
        return ResponseEntity.ok(ApiResponse.success("Xóa giỏ hàng thành công"));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long cartItemId,
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader,
            HttpServletRequest request) {
        Long userId = parseUserIdHeader(userIdHeader);
        String guestId = (userId == null) ? getGuestId(request) : null;
        log.info("API DELETE /cart/{} - userId: {}, guestId: {}", cartItemId, userId, guestId);

        if (userId == null && guestId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Không thể xóa khỏi giỏ hàng khi chưa xác định người dùng hoặc khách."));
        }

        cartService.removeItemFromCart(userId, guestId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi giỏ hàng thành công"));
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<Void>> mergeCart(
            @RequestHeader("X-Guest-ID") String guestId,
            @RequestHeader(name = "X-User-ID", required = true) String  userIdHeader) {

        Long userId = parseUserIdHeader(userIdHeader); // Chuyển đổi an toàn
        log.info("API POST /cart/merge - userId: {}, guestId: {}", userId, guestId);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Yêu cầu đăng nhập để gộp giỏ hàng."));
        }
        if (guestId == null || guestId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Thiếu Guest ID để thực hiện gộp."));
        }

        cartService.mergeGuestCartToUser(guestId, userId);
        return ResponseEntity.ok(ApiResponse.success("Gộp giỏ hàng thành công"));
    }

    // Helper để chuyển đổi header sang Long một cách an toàn
    private Long parseUserIdHeader(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            log.warn("Invalid X-User-ID format in header: {}. Ignoring.", userIdHeader);
            return null; // Trả về null nếu không parse được
        }
    }

}