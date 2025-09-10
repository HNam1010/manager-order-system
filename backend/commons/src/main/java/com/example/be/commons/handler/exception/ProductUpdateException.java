package com.example.be.commons.handler.exception;

public class ProductUpdateException extends RuntimeException {
    public ProductUpdateException(String message) {
        super(message);
    }

    public ProductUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
