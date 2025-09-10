package com.example.order_service.repository;


import com.example.order_service.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param; // THÊM DÒNG NÀY
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // Hoặc bạn có thể dùng @Query để rõ ràng hơn (tùy chọn)
     @Query("SELECT od FROM OrderDetail od WHERE od.order.serialId = :orderId")
     List<OrderDetail> findByOrderIdQuery(@Param("orderId") Long orderId);

}
