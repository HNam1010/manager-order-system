export interface ProductType {
  serialId: number;
  name: string;
}

// Interface Product (Khớp với Entity Product và dữ liệu trả về mong muốn từ API)
export interface Product {
  serialId: number; // BIGSERIAL -> Long -> number (JS không có Long riêng)
  name: string; // Khớp với name
  brand?: string; // Khớp với brand (Optional)
  imagePath?: string; // Khớp với imagePath (Optional) - Sẽ là URL ảnh
  price: number; // BigDecimal/NUMERIC -> number
  updateDate?: string | Date; // LocalDateTime -> string (ISO) hoặc Date
  quantity: number; // BIGINT -> Long -> number
  description?: string; // Khớp với description (Optional)
  createdDate?: string | Date; // LocalDateTime -> string (ISO) hoặc Date
  productTypeId: number; // ID của ProductType (Hoặc Long nếu backend là Long)
  productTypeName?: string; // Tên của ProductType (Nếu API trả về)
  userId: number; // ID của User (Integer -> number)
  size?: string;
}

// Interface ProductCreateRequest (Khớp với ProductRequest backend)

export interface ProductCreateRequest {
  name: string;
  brand?: string;
  // imagePath sẽ được xử lý riêng, không gửi trực tiếp trong JSON này
  price: number; // Gửi number
  quantity: number; // Gửi number
  description?: string;
  productTypeId: number; // Gửi number (Hoặc Long nếu backend là Long)
  userId: number; // Gửi number
  size?: string;
}

// Interface ProductUpdateRequest (Khớp logic cập nhật, có thể dựa trên ProductRequest)
export interface ProductUpdateRequest {
  name?: string;
  brand?: string;
  price?: number;
  quantity?: number;
  description?: string;
  productTypeId?: number; // Cho phép sửa loại?
  userId?: number; // Có cần gửi lại userId khi cập nhật? Thường là không.
  size?: string;
}
