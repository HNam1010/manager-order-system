package com.example.product_service.controller;


import com.example.be.commons.ApiResponse;
import com.example.be.commons.handler.exception.ForbiddenAccessException;
import com.example.be.commons.handler.exception.ResourceNotFoundException;
import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.UpdateStockRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.service.servicerepo.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Để xác định kiểu của file upload
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products") // Base path cho Product API
public class ProductController {

    private final ProductService productService; // Sử dụng interface

    // Khai báo Logger thủ công
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ObjectMapper objectMapper;

    // Constructor injection
    public ProductController(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }


    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(page = 0, size = 9, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(name = "typeId", required = false) Long productTypeId
    ) {
        log.info("Received request for getAllProducts - Page: {}, Size: {}, Sort: {}, Search: '{}', TypeId: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), search, productTypeId);

        // Gọi service - Service đã trả về Page<ProductResponse>
        Page<ProductResponse> productResponsePage = productService.getAllProducts(pageable, search, productTypeId);

        try {
            String jsonTest = objectMapper.writeValueAsString(productResponsePage);
            log.info("Manual serialization successful. Preview: {}", jsonTest.substring(0, Math.min(jsonTest.length(), 500))); // Log một phần JSON
        } catch (Exception e) {
            log.error("!!! ERROR DURING MANUAL SERIALIZATION of Page<ProductResponse> !!!", e); // Log lỗi chi tiết
            // Có thể trả về lỗi 500 ở đây để frontend biết rõ hơn
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Hoặc trả về một Page rỗng/đối tượng lỗi
        }

        // Trả về ResponseEntity
        return ResponseEntity.ok(productResponsePage); // trả về Page trực tiếp
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        try {
            // Gọi service - Service đã trả về ProductResponse
            ProductResponse productResponse = productService.getProductById(id);

            // Tạo ApiResponse thành công
            ApiResponse<ProductResponse> response = new ApiResponse<>(
                    true,
                    "Lấy chi tiết sản phẩm thành công",
                    productResponse //Sử dụng trực tiếp kết quả từ service
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) { // Bắt Exception cụ thể hơn nếu cần (vd: ResourceNotFoundException từ service)
            log.error("Error getting product by ID {}: {}", id, e.getMessage(), e);
            // Tạo ApiResponse lỗi
            ApiResponse<ProductResponse> response = new ApiResponse<>(false, e.getMessage(), null);
            // Xác định HttpStatus phù hợp (NOT_FOUND nếu là ResourceNotFoundException)
            HttpStatus status = (e instanceof com.example.be.commons.handler.exception.ResourceNotFoundException)
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.INTERNAL_SERVER_ERROR; // Hoặc BAD_REQUEST tùy lỗi
            return ResponseEntity.status(status).body(response);
        }
    }

    //cần quyền admin
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestPart("product") ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader(name = "X-User-ID", required = true) Long userId, // Lấy UserID người tạo
            @RequestHeader(name = "X-User-Roles", required = true) List<String> roles
    ){
        log.info("API POST /products - userId: {}, roles: {}", userId, roles);
        //KIỂM TRA QUYỀN ADMIN
        if (!roles.contains("ROLE_ADMIN")) {
            throw new ForbiddenAccessException("Yêu cầu quyền Admin để thực hiện hành động này.");
        }else { log.info("No image file received for create."); }

        productRequest.setUserId(userId); // Gán userId vào request để service lưu lại

        try {
            ProductResponse createdProduct = productService.createProduct(productRequest, imageFile);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(createdProduct.getSerialId())
                    .toUri();
            ApiResponse<ProductResponse> response = ApiResponse.success("Tạo sản phẩm thành công", createdProduct);
            return ResponseEntity.created(location).body(response);
        } catch (Exception e) {
            log.error("Lỗi khi tao sản phẩm bởi id {}: {}", userId, e.getMessage(), e);
            e.printStackTrace(); // In stack trace ra log để gỡ lỗi
            // Nên có GlobalExceptionHandler để xử lý các exception này
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Lỗi khi tạo sản phẩm: " + e.getMessage()));
        }
    }


    //PUT Endpoint (Cần quyền Admin)
    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart(value="product") ProductRequest productRequest, // Dùng ProductRequest vì cần cả userId
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader(name = "X-User-ID", required = true) Long userId, // Người thực hiện update
            @RequestHeader(name = "X-User-Roles", required = true) List<String> roles) {

        log.info("API PUT /products/{} - userId: {}, roles: {}", id, userId, roles);

        // KIỂM TRA QUYỀN ADMIN
        if (!roles.contains("ROLE_ADMIN")) {
            throw new ForbiddenAccessException("Yêu cầu quyền Admin để thực hiện hành động này.");
        }

        productRequest.setUserId(userId);

        try {
            ProductResponse updatedProduct = productService.updateProduct(id, productRequest, imageFile);
            ApiResponse<ProductResponse> response = ApiResponse.success("Cập nhật sản phẩm thành công", updatedProduct);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating product ID {} by user {}: {}", id, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Lỗi khi cập nhật sản phẩm: " + e.getMessage()));
        }
    }


    //cần quyền admin
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(name = "X-User-Roles", required = true) List<String> roles) {

        log.info("API DELETE /products/{} - roles: {}", id, roles);
        // KIỂM TRA QUYỀN ADMIN
        if (!roles.contains("ROLE_ADMIN")) {
            throw new ForbiddenAccessException("Yêu cầu quyền Admin để thực hiện hành động này.");
        }

        try {
            productService.deleteProduct(id);
            ApiResponse<Void> response = ApiResponse.success("Xóa sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting product ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi máy chủ nội bộ khi xóa sản phẩm."));
        }
    }


    // ENDPOINT MỚI: Xử lý giảm tồn kho hàng loạt
    // Chỉ Admin hoặc Service nội bộ được gọi (có thể thêm bảo mật nếu cần)
    @PutMapping("/stock/decrease")      // Thành dòng này
    public ResponseEntity<ApiResponse<Void>> decreaseStock(
            @RequestBody List<UpdateStockRequest> updateRequests) {
        log.info("API PATCH /products/stock/decrease - Received {} stock update requests", updateRequests.size());
        try {
            // Gọi phương thức mới trong ProductService để xử lý logic
            productService.decreaseStock(updateRequests);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật tồn kho thành công."));
        } catch (Exception e) {
            // Bắt các lỗi có thể xảy ra (vd: ResourceNotFound, IllegalState nếu số lượng âm)
            log.error("Error processing stock decrease request: {}", e.getMessage(), e);
            // GlobalExceptionHandler sẽ xử lý và trả về lỗi chi tiết
            if (e instanceof ResourceNotFoundException || e instanceof IllegalStateException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi máy chủ khi cập nhật tồn kho: " + e.getMessage()));
        }
    }
}
