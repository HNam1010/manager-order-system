export interface LoginRequest {
    username?: string | null; // Dùng ? và null nếu dùng ReactiveForms reset
    password?: string | null;
}

export interface RegisterRequest {
    username?: string | null;
    email?: string | null;
    password?: string | null;
    role?: string; // Hoặc string[]
}

export interface JwtResponse {
    token: string;
    type?: string; // Thường là 'Bearer'
    id: number; // Hoặc kiểu Long nếu backend là Long
    username: string;
    email: string;
    roles: string[]; // Danh sách tên các role
}

// Interface đơn giản để lưu thông tin user trong Storage/BehaviorSubject
export interface UserInfo {
    id: number;
    username: string;
    email: string;
    roles: string[];
    phone?: string;
    address?: string;
    birthDay?: string;
    fullName?: string;
}

export interface UserProfileUpdateRequest {
    email?: string;     // Cho phép sửa email (optional)
    phone?: string;     // Cho phép sửa số điện thoại (optional)
    address?: string;   // Cho phép sửa địa chỉ (optional)
    birthDay?: string;  // Cho phép sửa ngày sinh (gửi dạng YYYY-MM-DD) (optional)
    fullName: string
    newPassword?: string,   // Có sửa password không
}

//thêm interface quên mật khẩu 
export interface ResetPasswordRequest {
    username: string;
    email: string;
    newPassword?: string; // Mật khẩu mới
  }