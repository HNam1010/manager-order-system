package com.example.be.commons.handler.mapper;

import com.example.be.commons.handler.models.ErrorMessage;
import com.example.be.commons.handler.models.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Tạo một errorId duy nhất cho mỗi yêu cầu lỗi
        String errorId = UUID.randomUUID().toString();

        // Danh sách chứa thông điệp lỗi
        List<ErrorMessage> listErrorMessage = new ArrayList<>();

        // Lặp qua tất cả các lỗi và tạo ErrorMessage cho từng trường
        ex.getBindingResult().getAllErrors().forEach(error -> {
            // Lấy tên trường và thông điệp lỗi
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();

            // Tạo ErrorMessage cho từng lỗi và thêm vào danh sách
            ErrorMessage errorMsg = new ErrorMessage(null, fieldName, errorMessage);
            listErrorMessage.add(errorMsg);
        });

        // Tạo ErrorResponse và trả về phản hồi lỗi với mã trạng thái HTTP 400
        ErrorResponse errorResponse = new ErrorResponse(errorId, listErrorMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
