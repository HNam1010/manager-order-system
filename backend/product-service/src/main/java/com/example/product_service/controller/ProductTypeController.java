package com.example.product_service.controller;

import com.example.be.commons.ApiResponse;
import com.example.product_service.dto.request.ProductTypeRequest;
import com.example.product_service.dto.response.ProductTypeResponse;
import com.example.product_service.service.servicerepo.ProductTypeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.data.domain.Pageable;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/product-types")
public class ProductTypeController {
    private final ProductTypeService productTypeService;

    //constructor
    public ProductTypeController(ProductTypeService productTypeService) {
        this.productTypeService = productTypeService;
    }

    @PostMapping
    public ResponseEntity<ProductTypeResponse> createProductType(
            @Valid @RequestBody ProductTypeRequest request
    ) {
        ProductTypeResponse created = productTypeService.createProductType(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getSerialId())
                .toUri();

        ApiResponse<ProductTypeResponse> response = new ApiResponse<>(
                true,
                "Đã tạo loại sản phẩm thành công",
                created
        );

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductTypeResponse> getById(@PathVariable Integer id) {
        ProductTypeResponse response = productTypeService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductTypeResponse>> getAll(
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {
        Page<ProductTypeResponse> page = productTypeService.getAll(pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductTypeResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ProductTypeRequest request
    ) {
        ProductTypeResponse updated = productTypeService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
