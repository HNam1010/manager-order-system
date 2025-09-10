package com.example.be.commons.handler.controllerad;


import com.example.be.commons.ApiResponse; // Sử dụng ApiResponse của bạn
import com.example.be.commons.handler.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // Cho validation
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest; // Thêm WebRequest

import java.util.stream.Collectors; // Thêm Collectors

@RestControllerAdvice // Đánh dấu đây là một Global Exception Handler cho REST Controllers
public class OrderServiceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceExceptionHandler.class);

    @ExceptionHandler(OutOfStockException.class) // Bắt cụ thể OutOfStockException
    public ResponseEntity<ApiResponse<Object>> handleOutOfStockException(OutOfStockException ex, WebRequest request) {
        log.warn("Handling OutOfStockException: {}", ex.getMessage());
        // Trả về lỗi 400 Bad Request với thông điệp từ exception
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // Mã lỗi 400
                .body(ApiResponse.error(ex.getMessage())); // Thông điệp lỗi cho frontend
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmptyCartException(EmptyCartException ex, WebRequest request) {
        log.warn("Handling EmptyCartException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleProductNotFoundException(ProductNotFoundException ex, WebRequest request) {
        log.warn("Handling ProductNotFoundException: {}", ex.getMessage());
        // Có thể trả về 404 Not Found hoặc 400 Bad Request tùy ngữ cảnh
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class) // Bắt lỗi không tìm thấy đơn hàng/status
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Handling ResourceNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenAccessException.class) // Bắt lỗi không có quyền (ví dụ token khách sai)
    public ResponseEntity<ApiResponse<Object>> handleForbiddenAccessException(ForbiddenAccessException ex, WebRequest request) {
        log.warn("Handling ForbiddenAccessException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class) // Bắt lỗi yêu cầu thiếu thông tin
    public ResponseEntity<ApiResponse<Object>> handleInvalidRequestException(InvalidRequestException ex, WebRequest request) {
        log.warn("Handling InvalidRequestException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ServiceUnavailableException.class) // Lỗi gọi service khác
    public ResponseEntity<ApiResponse<Object>> handleServiceUnavailableException(ServiceUnavailableException ex, WebRequest request) {
        log.error("Handling ServiceUnavailableException: {}", ex.getMessage(), ex.getCause()); // Log cả cause
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse.error(ex.getMessage()));
    }

    // Xử lý lỗi validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Handling Validation Exception: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Dữ liệu không hợp lệ: " + errors));
    }


    // Xử lý các lỗi chung khác (nên đặt cuối cùng)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Handling generic Exception: {}", ex.getMessage(), ex); // Log stack trace cho lỗi không mong muốn
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Đã có lỗi xảy ra phía máy chủ. Vui lòng thử lại sau."));
    }
}
