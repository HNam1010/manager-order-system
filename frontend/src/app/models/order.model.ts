import { Page, Pageable, Sort } from './page.model'; // Import từ file chung

//  Tái xuất các kiểu Page
export type { Page, Pageable, Sort };

// Enum trạng thái 
export enum StatusEnum {
    PENDING_CONFIRMATION = 'PENDING_CONFIRMATION',
    CONFIRMED = 'CONFIRMED',
    IN_DELIVERY = 'IN_DELIVERY',
    CANCELLED = 'CANCELLED',
    COMPLETED = 'COMPLETED'
}

// Interface cho một mục sản phẩm trong đơn hàng 
export interface OrderItemResponse {
    orderDetailId: number; // ID của bản ghi order_details 
    productId: number;      // ID gốc của Product
    productName: string;    // Snapshot
    productBrand?: string;   // Snapshot
    productImagePath?: string; // Snapshot đường dẫn ảnh
    productPrice: number;   // Snapshot giá lúc mua
    quantity: number;
    totalPrice: number;     // quantity * productPrice

}

// Interface cho thông tin đơn hàng trả về 
export interface OrderResponse {
    serialId: number;       // ID của Orders
    orderCode?: string;     // Thêm mã đơn hàng backend trả về
    userId?: number;         // ID người dùng (có thể null nếu là guest)
    customerName: string;    // Thông tin giao hàng
    phoneNumber: string;
    shippingAddress: string;
    email?: string;
    orderNotes?: string;
    totalAmount: number;     // Tổng tiền 
    orderDate: string | Date; // Ngày đặt hàng
    updatedAt?: string | Date; // Thêm ngày cập nhật (nếu backend trả về)
    statusId: number;        // ID trạng thái
    statusDescription?: string; // Tên/Mô tả trạng thái
    statusCode?: string;     // Mã trạng thái (ví dụ: "CONFIRMED") - khớp mapper
    paymentMethod?: string;   // Phương thức thanh toán
    items: OrderItemResponse[]; // Danh sách các mục sản phẩm

    guestToken?: string; // Token tạm thời
}

export interface UpdateOrderStatusRequest {
    newStatusId: number;
}

// DTO đặt hàng 
export interface PlaceOrderRequest {
    guestId?: string; // Thêm trường này tạo id cho người k đăng nhập
    customerName: string;
    shippingAddress: string;
    phoneNumber?: string;
    email?: string;
    orderNotes?: string;
    paymentMethod: string;
}

// Thông tin ngân hàng 
export interface BankInfo {
    accountName: string;
    accountNumber: string;
    bankName: string;
    bankBranch?: string;
    transferContentPrefix?: string;
}