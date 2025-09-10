export interface Sort {
  empty: boolean;
  sorted: boolean;
  unsorted: boolean;
}
export interface Pageable {
  pageNumber: number;
  pageSize: number;
  sort: Sort;
  offset: number;
  paged: boolean;
  unpaged: boolean;
}
export interface Page<T> {
  content: T[];
  pageable: Pageable;
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // Trang hiện tại (0-based)
  sort: Sort;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface User {
  id: number; // Khớp id (Long -> number)
  username: string; // Khớp username
  email: string; // Khớp email
  roles: string[]; // Khớp roles (List<String> -> string[])
  birthDay?: string; // Thêm nếu backend trả về, để string nếu dùng pipe date
  address?: string;
}

export interface Role {
  id: number; // Khớp id (Integer -> number)
  name: string; // Khớp name (String -> string - ví dụ: "ADMIN", "CUSTOMER")
}

export interface UserCreateRequest {
  // Hoặc đổi tên thành RegisterRequest
  username: string;
  email: string;
  password?: string; // Bắt buộc khi tạo
  birthDay?: string;
  address?: string;
  role: string; // Tên Role dạng string ("ADMIN" hoặc "CUSTOMER")
}

export interface UserUpdateRequest {
  email?: string;
  birthDay?: string; // Nếu cho sửa
  address?: string; // Nếu cho sửa
  role?: string; // Tên Role mới 
}


export interface UserProfileUpdateRequest {
  email?: string;
  phone?: string;
  address?: string;
  birthDay?: string;
  currentPassword?: string; // Thêm nếu cho đổi mật khẩu
  newPassword?: string; // Thêm nếu cho đổi mật khẩu
}

export interface UserResponse {
  id: number; // Khớp id (Long -> number)
  username: string; // Khớp username
  email: string; // Khớp email
  roles: string[]; // Khớp roles (List<String> -> string[])
  phone?: string;
  address?: string;
  birthDay?: string | Date; // Backend có thể trả về string ISO hoặc bạn tự map sang Date
  createdAt?: string | Date;
  updatedAt?: string | Date;
}
