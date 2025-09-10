package com.example.product_service.service.servicerepo;

import com.example.product_service.dto.request.ProductTypeRequest;
import com.example.product_service.dto.response.ProductTypeResponse;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;

public interface ProductTypeService {
    ProductTypeResponse createProductType(ProductTypeRequest request);
    ProductTypeResponse getById(Integer id);
    Page<ProductTypeResponse> getAll(Pageable pageable);

    Page<ProductTypeResponse> getAll(org.springframework.data.domain.Pageable pageable);

    ProductTypeResponse update(Integer id, ProductTypeRequest request);
    void delete(Integer id);
}