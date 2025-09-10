package com.example.product_service.repository;

import com.example.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productType pt", // Bỏ fetch user
            countQuery = "SELECT COUNT(DISTINCT p) FROM Product p")
    Page<Product> findAllWithDetails(Pageable pageable); // Đổi tên nếu muốn

    @Query(value = "SELECT DISTINCT p.* FROM products p " + // Dùng tên bảng products
            "LEFT JOIN product_types pt ON p.product_type_id = pt.serial_id " + // Dùng tên bảng/cột thực tế
            "WHERE (:search IS NULL OR p.name ILIKE CONCAT('%', :search, '%') OR p.description ILIKE CONCAT('%', :search, '%')) " +
            "AND (:typeId IS NULL OR pt.serial_id = :typeId)",
            countQuery = "SELECT COUNT(DISTINCT p.serial_id) FROM products p " + // Dùng tên bảng products
                    "LEFT JOIN product_types pt ON p.product_type_id = pt.serial_id " +
                    "WHERE (:search IS NULL OR p.name ILIKE CONCAT('%', :search, '%') OR p.description ILIKE CONCAT('%', :search, '%')) " +
                    "AND (:typeId IS NULL OR pt.serial_id = :typeId)",
            nativeQuery = true)
    Page<Product> searchAndFilterProducts(@Param("search") String search,
                                          @Param("typeId") Long typeId,
                                          Pageable pageable);

}