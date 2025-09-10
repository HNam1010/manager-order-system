package com.example.cart_service.service.servicerepo;


import com.example.cart_service.dto.request.AddToCartRequest;
import com.example.cart_service.dto.reponse.CartItemDTO;
import com.example.cart_service.dto.request.UpdateCartItemRequest;
import java.util.List;

public interface CartService {
    // Lấy giỏ hàng (cần userId hoặc guestId)
    List<CartItemDTO> getCart(Long userId, String guestId);

    // Thêm vào giỏ hàng
    CartItemDTO addItemToCart(Long userId, String guestId, AddToCartRequest request);

    // Cập nhật số lượng
    CartItemDTO updateItemQuantity(Long userId, String guestId, Long cartItemId, UpdateCartItemRequest request);

    // Xóa item
    void removeItemFromCart(Long userId, String guestId, Long cartItemId);

    // Xóa toàn bộ giỏ hàng (khi đặt hàng xong)
    void clearCart(Long userId, String guestId);

    // Lấy số lượng item trong giỏ (cho header)
    long getCartItemCount(Long userId, String guestId);

    // (Tùy chọn) Merge giỏ hàng guest vào user khi đăng nhập
    void mergeGuestCartToUser(String guestId, Long userId);


}
