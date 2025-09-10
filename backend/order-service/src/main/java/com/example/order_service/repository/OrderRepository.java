package com.example.order_service.repository;

import com.example.order_service.entity.Order;
import org.springframework.data.repository.query.Param; // THÊM DÒNG NÀY
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> { // Khóa chính của Order là Long

    // Tìm đơn hàng theo userId với phân trang và sắp xếp (theo ngày đặt hàng giảm dần)
    // Sử dụng JOIN FETCH để tải trạng thái (status) cùng lúc, tránh N+1 query
    @Query(value = "SELECT o FROM Order o LEFT JOIN FETCH o.status WHERE o.userId = :userId",
            countQuery = "SELECT count(o) FROM Order o WHERE o.userId = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable); // <-- THÊM @Param ở đây

    // Có thể thêm các phương thức tìm kiếm khác nếu cần (theo mã đơn hàng, theo trạng thái,...)
    Optional<Order> findByOrderCode(String orderCode);

    // Lấy chi tiết đơn hàng kèm cả details và status (cho xem chi tiết)
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.status LEFT JOIN FETCH o.orderDetails od WHERE o.serialId = :orderId")
    Optional<Order> findByIdWithDetailsAndStatus(@Param("orderId") Long orderId);

    // Lấy chi tiết đơn hàng của một user cụ thể kèm details và status
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.status LEFT JOIN FETCH o.orderDetails od WHERE o.serialId = :orderId AND o.userId = :userId")
    Optional<Order> findByIdAndUserIdWithDetailsAndStatus(@Param("orderId") Long orderId, @Param("userId") Long userId);

    // Lọc theo ID của trạng thái (giả sử Order có liên kết đến OrderStatus tên là 'status')
    Page<Order> findByStatus_SerialId(Integer statusId, Pageable pageable);

    // Phương thức lấy tất cả (có thể giữ hoặc bỏ nếu findByStatus_SerialId xử lý được cả case statusId=null)
    @Query("SELECT o FROM Order o JOIN FETCH o.status") // fetch để tránh N+1
    Page<Order> findAllWithStatus(Pageable pageable);
}
