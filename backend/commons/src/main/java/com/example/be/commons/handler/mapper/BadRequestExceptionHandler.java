package com.example.be.commons.handler.mapper;

import com.example.be.commons.handler.exception.BadRequestException;
import com.example.be.commons.handler.models.ErrorMessage;
import com.example.be.commons.handler.models.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.util.UUID;

@ControllerAdvice
public class BadRequestExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorMessage errorMessage = new ErrorMessage(ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
