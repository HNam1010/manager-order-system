package com.example.be.commons.handler.mapper;

import com.example.be.commons.handler.exception.ServiceException;
import com.example.be.commons.handler.models.ErrorMessage;
import com.example.be.commons.handler.models.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class ServiceExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(ServiceException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorMessage errorMessage = new ErrorMessage(ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
