package com.example.cart_service.repository;


import com.example.cart_service.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Tìm tất cả item theo userId
    List<CartItem> findByUserId(Long userId);

    // Tìm tất cả item theo guestId
    List<CartItem> findByGuestId(String guestId);

    // Tìm item cụ thể theo user và product
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    // Tìm item cụ thể theo guest và product
    Optional<CartItem> findByGuestIdAndProductId(String guestId, Long productId);

    // Xóa tất cả item theo userId
    void deleteByUserId(Long userId);

    // Xóa tất cả item theo guestId
    void deleteByGuestId(String guestId);
}