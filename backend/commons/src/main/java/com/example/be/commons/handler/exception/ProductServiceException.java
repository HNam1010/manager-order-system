package com.example.be.commons.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Hoặc BAD_GATEWAY nếu phù hợp hơn
public class ProductServiceException extends RuntimeException {
    public ProductServiceException(String message) {
        super(message);
    }
    public ProductServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}