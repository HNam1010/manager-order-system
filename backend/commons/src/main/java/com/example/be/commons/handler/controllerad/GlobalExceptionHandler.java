package com.example.be.commons.handler.controllerad;

import com.example.be.commons.ApiResponse;
import com.example.be.commons.handler.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Import Collectors nếu chưa có
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger exceptionHandlerLogger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // Handler cho @Valid - Sửa lại dùng ApiResponse
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
          MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> "`" + error.getField() + "`: " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    String message = "Dữ liệu không hợp lệ: " + errors;
    exceptionHandlerLogger.warn("Validation errors: {}", errors);
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message)); // Sử dụng ApiResponse
  }

  // Handler cho ResourceNotFoundException - Sửa lại dùng ApiResponse
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
    exceptionHandlerLogger.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage())); // Sử dụng ApiResponse
  }

  // Handler cho BadRequestException - Sửa lại dùng ApiResponse
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse<Object>> handleBadRequestException(BadRequestException ex, WebRequest request) {
    exceptionHandlerLogger.warn("Bad Request: {}", ex.getMessage());
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage())); // Sử dụng ApiResponse
  }

  // Handler cho MethodArgumentTypeMismatchException - Sửa lại dùng ApiResponse
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
    String message = String.format("Tham số '%s' yêu cầu kiểu '%s' nhưng nhận được giá trị '%s'.",
            ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "[unknown type]", ex.getValue());
    exceptionHandlerLogger.warn("Type Mismatch: {}", message);
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message)); // Sử dụng ApiResponse
  }

  // --- Các Handler cho Exception Nghiệp Vụ (Giữ nguyên vì đã đúng) ---

  @ExceptionHandler(OutOfStockException.class)
  public ResponseEntity<ApiResponse<Object>> handleOutOfStockException(OutOfStockException ex, WebRequest request) {
    // !!! THÊM DÒNG NÀY ĐỂ DEBUG !!!
    exceptionHandlerLogger.warn("--- !!! EXCEPTION HANDLER CALLED: Handling OutOfStockException: {} !!! ---", ex.getMessage());
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(EmptyCartException.class)
  public ResponseEntity<ApiResponse<Object>> handleEmptyCartException(EmptyCartException ex, WebRequest request) {
    exceptionHandlerLogger.warn("Handling EmptyCartException: {}", ex.getMessage());
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleProductNotFoundException(ProductNotFoundException ex, WebRequest request) {
    exceptionHandlerLogger.warn("Handling ProductNotFoundException: {}", ex.getMessage());
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(ForbiddenAccessException.class)
  public ResponseEntity<ApiResponse<Object>> handleForbiddenAccessException(ForbiddenAccessException ex, WebRequest request) {
    exceptionHandlerLogger.warn("Handling ForbiddenAccessException: {}", ex.getMessage());
    return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<ApiResponse<Object>> handleInvalidRequestException(InvalidRequestException ex, WebRequest request) {
    exceptionHandlerLogger.warn("Handling InvalidRequestException: {}", ex.getMessage());
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(OrderProcessingException.class)
  public ResponseEntity<ApiResponse<Object>> handleOrderProcessingException(OrderProcessingException ex, WebRequest request) {
    exceptionHandlerLogger.error("Handling OrderProcessingException: {}", ex.getMessage(), ex);
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ApiResponse<Object>> handleServiceUnavailableException(ServiceUnavailableException ex, WebRequest request) {
    exceptionHandlerLogger.error("Handling ServiceUnavailableException: {}", ex.getMessage(), ex);
    return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error(ex.getMessage()));
  }

  // --- Handler Chung ---
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, WebRequest request) {
    exceptionHandlerLogger.error("Unhandled exception caught by generic handler: ", ex);
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Đã xảy ra lỗi không mong muốn từ máy chủ."));
  }

}