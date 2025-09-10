package com.example.auth.dto.reponse;

public class MessageResponse {
    private String message;

    // Constructor để dễ dàng tạo đối tượng response
    public MessageResponse(String message) {
        this.message = message;
    }

    // Getter and Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
