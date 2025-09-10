package com.example.be.commons;

public class ApiResponse<T> {


    private boolean success; // Trạng thái thành công/thất bại
    private String message;  // Thông báo kèm theo
    private T data;          // Dữ liệu thực tế (có thể là null)


    public ApiResponse() {
    }

    // Constructor đầy đủ
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Constructor cho trường hợp không có data (ví dụ: lỗi, hoặc delete thành công)
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    // --- Static factory methods (Tùy chọn - giúp code dễ đọc hơn) ---
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }


    // Getters and Setters (Bắt buộc nếu không dùng Lombok)
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}