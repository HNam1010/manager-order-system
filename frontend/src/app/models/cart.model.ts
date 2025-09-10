// Interface cho một mục trong giỏ hàng (khớp CartItemResponse)
export interface CartItem {
    cartItemId: number;
    productId: number;
    productName: string;
    productImageUrl?: string;
    productPrice: number;
    quantity: number;
    totalPrice: number; // Tổng tiền của item này
    productBrand?: string; // <-- THÊM DÒNG NÀY
    productStock?: number; // tồn kho
}

// Interface cho request thêm vào giỏ (khớp AddToCartRequest)
export interface AddToCartRequest {
    productId: number;
    quantity: number;
    // note?: string;
}

// Interface cho request cập nhật số lượng (khớp UpdateCartItemRequest)
export interface UpdateCartItemRequest {
    quantity: number;
}
