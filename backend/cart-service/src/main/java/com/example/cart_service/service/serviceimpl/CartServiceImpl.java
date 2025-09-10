package com.example.cart_service.service.serviceimpl;

import com.example.be.commons.ApiResponse;
import com.example.be.commons.handler.exception.CartItemNotFoundException;
import com.example.be.commons.handler.exception.ProductServiceException;
import com.example.cart_service.dto.reponse.CartItemDTO;
import com.example.cart_service.dto.reponse.ProductDTO;
import com.example.cart_service.dto.request.AddToCartRequest;
import com.example.cart_service.dto.request.UpdateCartItemRequest;
import com.example.cart_service.service.servicerepo.ProductServiceClient;
import com.example.cart_service.entity.CartItem;
import com.example.cart_service.repository.CartItemRepository;
import com.example.cart_service.service.servicerepo.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Override
    public List<CartItemDTO> getCart(Long userId, String guestId) {
        log.info("Fetching cart for userId: {}, guestId: {}", userId, guestId);
        List<CartItem> items;

        if (userId != null) {
            items = cartItemRepository.findByUserId(userId);
        } else if (guestId != null) {
            items = cartItemRepository.findByGuestId(guestId);
        } else {
            log.warn("Attempted to get cart without userId or guestId");
            return new ArrayList<>(); // Trả về giỏ hàng rỗng nếu không có định danh
        }
        log.info("Found {} items in DB", items.size());

        List<CartItemDTO> dtos = new ArrayList<>();
        for (CartItem item : items) {
            try {
                log.debug("Fetching product details for productId: {}", item.getProductId());
                ApiResponse<ProductDTO> productResponse = productServiceClient.getProductById(item.getProductId());
                if (productResponse != null && productResponse.isSuccess() && productResponse.getData() != null) {
                    ProductDTO product = productResponse.getData();
                    dtos.add(mapToCartItemDTO(item, product));
                } else {
                    log.warn("Product details not found or failed for productId: {}. Message: {}", item.getProductId(), productResponse != null ? productResponse.getMessage() : "N/A");
                }
            } catch (Exception e) {
                log.error("Error calling Product Service for productId: {}", item.getProductId(), e);
            }
        }
        log.info("Returning {} fully populated cart items", dtos.size());
        return dtos;
    }

    @Override
    @Transactional
    public CartItemDTO addItemToCart(Long userId, String guestId, AddToCartRequest request) {
        log.info("Adding item to cart. UserId: {}, GuestId: {}, ProductId: {}, Quantity: {}",
                userId, guestId, request.getProductId(), request.getQuantity());

        if (userId == null && guestId == null) {
            throw new IllegalArgumentException("Cần có userId hoặc guestId để thêm vào giỏ hàng.");
        }

        Optional<CartItem> existingItemOpt;
        if (userId != null) {
            existingItemOpt = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        } else {
            existingItemOpt = cartItemRepository.findByGuestIdAndProductId(guestId, request.getProductId());
        }

        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            log.info("Item exists, updating quantity to {}", cartItem.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setGuestId(guestId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            log.info("Item does not exist, creating new item.");
        }

        CartItem savedItem = cartItemRepository.save(cartItem);
        log.info("Đã lưu giỏ hàng vào ID: {}", savedItem.getCartItemId());

        ProductDTO product = fetchProductDetails(savedItem.getProductId(), "thêm vào giỏ");
        return mapToCartItemDTO(savedItem, product);
    }

    @Override
    @Transactional
    public CartItemDTO updateItemQuantity(Long userId, String guestId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("Updating quantity for cartItemId: {}. UserId: {}, GuestId: {}. New Quantity: {}",
                cartItemId, userId, guestId, request.getQuantity());

        CartItem cartItem = findCartItemByIdAndOwner(cartItemId, userId, guestId)
                .orElseThrow(() -> new CartItemNotFoundException("Không tìm thấy mục giỏ hàng ID: " + cartItemId + " cho người dùng này."));

        cartItem.setQuantity(request.getQuantity());

        CartItem updatedItem = cartItemRepository.save(cartItem);
        log.info("Đã cập nhật giỏ hàng với ID: {}", updatedItem.getCartItemId());

        ProductDTO product = fetchProductDetails(updatedItem.getProductId(), "cập nhật giỏ hàng");
        return mapToCartItemDTO(updatedItem, product);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long userId, String guestId, Long cartItemId) {
        log.info("Removing cartItemId: {}. UserId: {}, GuestId: {}", cartItemId, userId, guestId);

        CartItem cartItem = findCartItemByIdAndOwner(cartItemId, userId, guestId)
                .orElseThrow(() -> new CartItemNotFoundException("Không tìm thấy mục giỏ hàng ID: " + cartItemId + " để xóa."));

        cartItemRepository.delete(cartItem);
        log.info("Removed cart item ID: {}", cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId, String guestId) {
        log.info("Clearing cart for UserId: {}, GuestId: {}", userId, guestId);

        if (userId != null) {
            cartItemRepository.deleteByUserId(userId);
            log.info("Cleared cart for userId: {}", userId);
        } else if (guestId != null) {
            cartItemRepository.deleteByGuestId(guestId);
            log.info("Cleared cart for guestId: {}", guestId);
        } else {
            log.warn("Attempted to clear cart without userId or guestId");
        }
    }

    @Override
    public long getCartItemCount(Long userId, String guestId) {
        log.debug("Counting cart items for UserId: {}, GuestId: {}", userId, guestId);

        List<CartItem> items;
        if (userId != null) {
            items = cartItemRepository.findByUserId(userId);
        } else if (guestId != null) {
            items = cartItemRepository.findByGuestId(guestId);
        } else {
            return 0;
        }
        long count = items.stream().mapToInt(CartItem::getQuantity).sum(); // Đếm tổng số lượng sản phẩm
        // Hoặc chỉ đếm số dòng item: long count = items.size();
        log.debug("Total item quantity count: {}", count);
        return count;
    }

    // Helper tìm CartItem dựa trên ID và chủ sở hữu
    private Optional<CartItem> findCartItemByIdAndOwner(Long cartItemId, Long userId, String guestId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElse(null);

        if (item == null) return Optional.empty();

        // Kiểm tra quyền sở hữu
        if (userId != null && Objects.equals(item.getUserId(), userId)) {
            return Optional.of(item);
        }
        if (guestId != null && Objects.equals(item.getGuestId(), guestId)) {
            return Optional.of(item);
        }

        // Nếu không khớp user hoặc guest -> không được phép truy cập
        return Optional.empty();
    }

    // --- Helper Methods ---
    private List<CartItem> findCartItemsByOwner(Long userId, String guestId) {
        if (userId != null) {
            return cartItemRepository.findByUserId(userId);
        } else if (guestId != null) {
            return cartItemRepository.findByGuestId(guestId);
        } else {
            log.warn("Attempted to find cart items without userId or guestId");
            return new ArrayList<>();
        }
    }

    // Helper để gọi Product Service và xử lý lỗi/response
    private ProductDTO fetchProductDetails(Long productId, String actionContext) {
        try {
            log.debug("Fetching product details via Feign for productId: {} during action: {}", productId, actionContext);
            ApiResponse<ProductDTO> productResponse = productServiceClient.getProductById(productId);
            if (productResponse != null && productResponse.isSuccess() && productResponse.getData() != null) {
                return productResponse.getData();
            } else {
                String message = "Không thể lấy thông tin sản phẩm sau khi " + actionContext + ".";
                if(productResponse != null) message += " Reason: " + productResponse.getMessage();
                throw new ProductServiceException(message);
            }
        } catch (Exception e) {
            log.error("Error fetching product info via Feign for productId: {} during action: {}. Error: {}", productId, actionContext, e.getMessage());
            throw new ProductServiceException("Lỗi gọi Product Service sau khi " + actionContext + ".", e);
        }
    }

    // Hàm helper để map từ Entity + ProductDTO sang CartItemDTO
    private CartItemDTO mapToCartItemDTO(CartItem item, ProductDTO product) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(item.getCartItemId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());

        if (product != null) {
            dto.setProductName(product.getName());
            dto.setProductBrand(product.getBrand());
            dto.setProductPrice(product.getPrice());
            dto.setProductImageUrl(product.getImagePath());
            dto.setProductStock(product.getQuantity()); // Lấy tồn kho
            // Tính tổng tiền cho item này
            if (product.getPrice() != null && item.getQuantity() != null) {
                dto.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            } else {
                dto.setTotalPrice(BigDecimal.ZERO);
            }
        } else {
            // Xử lý nếu không có thông tin product
            dto.setProductName("N/A");
            dto.setProductPrice(BigDecimal.ZERO);
            dto.setTotalPrice(BigDecimal.ZERO);
        }
        return dto;
    }

    @Override
    @Transactional
    public void mergeGuestCartToUser(String guestId, Long userId) {

    }
}