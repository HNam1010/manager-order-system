package com.example.product_service.service.servicerepo;


import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.UpdateStockRequest;
import com.example.product_service.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    // Method for creating a product
    ProductResponse createProduct(ProductRequest productRequest, MultipartFile imageFile);

    // Method for getting a single product by ID
    ProductResponse getProductById(Long id);

    // Method for getting all products with pagination, search, and filter
    Page<ProductResponse> getAllProducts(Pageable pageable, String search, Long typeId);

    // Method for updating a product
    ProductResponse updateProduct(Long id, ProductRequest productRequest, MultipartFile imageFile);

    // Method for deleting a product
    void deleteProduct(Long id);

    void decreaseStock(List<UpdateStockRequest> updateRequests); // Phương thức mới

}