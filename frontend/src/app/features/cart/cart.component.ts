import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

// THÊM ReactiveFormsModule VÀ FormBuilder
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Subscription, finalize } from 'rxjs';
import { CartService } from '../../service/cart.service';
import { CartItem } from '../../models/cart.model';
import { OrderService } from '../../service/order.service';
import { PlaceOrderRequest, OrderResponse } from '../../models/order.model';
import { StorageService } from '../../core/services/storage.service';

import { HeaderComponent } from '../../layout/header/header.component';
import { FooterComponent } from '../../layout/footer/footer.component';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [
    HeaderComponent,
    FooterComponent,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})

export class CartComponent implements OnInit, OnDestroy {

  cartItems: CartItem[] = [];
  isLoading: boolean = false;
  errorMessage: string | null = null;
  totalAmount: number = 0; // Tổng tiền giỏ hàng
  isLoggedIn: boolean = false; // Thêm biến trạng thái đăng nhập
  isSubmitting: boolean = false;


  private cartSubscription: Subscription | null = null;
  private updateSubscription: Subscription | null = null;
  private removeSubscription: Subscription | null = null;

  //Inject THÊM BIẾN CHO FORM VÀ ĐẶT HÀNG
  checkoutForm: FormGroup; // Khai báo FormGroup
  isPlacingOrder: boolean = false; // Cờ cho trạng thái đang đặt hàng
  private placeOrderSub: Subscription | null = null; // Subscription cho việc đặt hàng


  // Base URL ảnh (giống HomeComponent)
  imageBaseUrl = 'http://localhost:8080/api/v1/products/images/';


  constructor(
    private cartService: CartService,
    private orderService: OrderService, // Inject OrderService
    private fb: FormBuilder,        // Inject FormBuilder
    private router: Router,
    private storageService: StorageService
  ) {
    // KHỞI TẠO 
    this.checkoutForm = this.fb.group({
      customerName: ['', [Validators.required, Validators.maxLength(100)]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^(0|\+84)[0-9]{9}$/)]], // Pattern VN Phone
      shippingAddress: ['', [Validators.required, Validators.maxLength(255)]],
      email: ['', [Validators.email, Validators.maxLength(100)]],
      orderNotes: ['', Validators.maxLength(500)],


      // THÊM paymentMethod VÀO ĐÂY
      paymentMethod: ['COD', Validators.required] // Mặc định là 'COD' và bắt buộc chọn

    });
  }


  ngOnInit(): void {
    this.loadCart();

    this.isLoggedIn = this.storageService.isLoggedIn(); // Lấy trạng thái đăng nhập ban đầu
  }

  ngOnDestroy(): void {
    this.cartSubscription?.unsubscribe();
    this.updateSubscription?.unsubscribe();
    this.removeSubscription?.unsubscribe();
    this.placeOrderSub?.unsubscribe(); // Hủy cả subscription đặt hàng
  }

  loadCart(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.cartSubscription = this.cartService.getCartItems().subscribe({
      next: (items) => {
        this.cartItems = items;
        this.calculateTotal(); // Tính tổng tiền sau khi tải
        this.isLoading = false;

        console.log('Cart items loaded:', this.cartItems);
      },
      error: (err) => {
        this.errorMessage = err.message || 'Không thể tải giỏ hàng.';
        this.isLoading = false;
        console.error('Error loading cart:', err);
      }
    });
  }

  // Tính tổng tiền giỏ hàng
  calculateTotal(): void {
    this.totalAmount = this.cartItems.reduce((sum, item) => {
      // Đảm bảo giá và số lượng là số hợp lệ trước khi tính
      const price = typeof item.productPrice === 'number' ? item.productPrice : 0;
      const quantity = typeof item.quantity === 'number' ? item.quantity : 0;
      return sum + (price * quantity); // Tính lại tổng tiền item ở đây cho chắc chắn
    }, 0);
  }

  // Xử lý khi thay đổi số lượng
  onQuantityChange(item: CartItem, event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    const newQuantityString = inputElement.value;
    let newQuantity: number | null = null;

    if (newQuantityString) { newQuantity = parseInt(newQuantityString, 10); }

    // 1. Chỉ Validate số lượng cơ bản (>= 1)
    if (newQuantity === null || isNaN(newQuantity) || newQuantity < 1) {
      alert("Số lượng không hợp lệ. Vui lòng nhập một số lớn hơn hoặc bằng 1.");
      inputElement.value = item.quantity.toString(); // Reset về giá trị cũ
      return;
    }

    // 2. Chỉ gọi API nếu số lượng thực sự thay đổi
    if (newQuantity !== item.quantity) {
      console.log(`Requesting quantity update for item ${item.cartItemId} to ${newQuantity}`);
      this.isLoading = true; // Có thể set loading riêng
      this.updateSubscription?.unsubscribe();
      this.updateSubscription = this.cartService.updateCartItem(item.cartItemId, newQuantity).subscribe({
        next: (updatedItem) => {
          console.log('Item quantity updated via API:', updatedItem);
          const index = this.cartItems.findIndex(i => i.cartItemId === updatedItem.cartItemId);
          if (index !== -1) {
            // Cập nhật item trong mảng 
            this.cartItems[index] = updatedItem;
            this.calculateTotal(); // Tính lại tổng tiền
          } else {
            this.loadCart(); // Lỗi không mong muốn
          }
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = `Lỗi cập nhật số lượng: ${err.message}`;
          alert(this.errorMessage);
          this.isLoading = false;
          // Khi API lỗi, reset về giá trị TRƯỚC KHI thay đổi
          inputElement.value = item.quantity.toString();
          console.error(err);
        }
      });
    } else {
      console.log(`Quantity not changed for item ${item.cartItemId}.`);
    }
  }

  updateQuantity(item: CartItem, change: number): void {
    const newQuantity = (item.quantity || 0) + change;
    if (newQuantity >= 1) {
      // Gọi API cập nhật
      this.isLoading = true; // Có thể dùng cờ loading riêng cho item
      this.updateSubscription = this.cartService.updateCartItem(item.cartItemId, newQuantity)
        .subscribe({
          // next và error xử lý như cũ, sau đó gọi this.loadCartItems() hoặc cập nhật local
          next: () => this.loadCart(), // Load lại toàn bộ giỏ hàng cho đơn giản
          error: (err) => { this.isLoading = false; },
        });
    } else if (newQuantity === 0) {
      // Nếu số lượng về 0, xóa sản phẩm
      this.removeItem(item.cartItemId);
    }
  }

  // HÀM SUBMIT ĐẶT HÀNG
  submitOrder(): void {
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      this.errorMessage = "Vui lòng điền đầy đủ thông tin bắt buộc.";
      alert(this.errorMessage);
      return;
    }
    if (this.cartItems.length === 0) {
      this.errorMessage = "Giỏ hàng trống.";
      alert(this.errorMessage);
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null; // Reset lỗi trước khi submit

    const orderData: PlaceOrderRequest = {
      customerName: this.checkoutForm.value.customerName,
      shippingAddress: this.checkoutForm.value.shippingAddress,
      phoneNumber: this.checkoutForm.value.phoneNumber,
      email: this.checkoutForm.value.email,
      orderNotes: this.checkoutForm.value.orderNotes || undefined,
      paymentMethod: this.checkoutForm.value.paymentMethod, // Gửi String
      guestId: undefined
    };

    if (!this.storageService.isLoggedIn()) {
      orderData.guestId = this.cartService.getCurrentGuestId() ?? undefined;
      console.log("Placing order as guest. Guest ID:", orderData.guestId);
      if (!orderData.guestId || !orderData.customerName || !orderData.email || !orderData.phoneNumber || !orderData.shippingAddress) {
        this.errorMessage = "Vui lòng điền đầy đủ thông tin khách hàng.";
        this.isSubmitting = false;
        alert(this.errorMessage);
        return;
      }
    } else {
      console.log("Placing order as logged-in user.");
      const currentUser = this.storageService.getUser();
      if (currentUser) {
        if (!orderData.customerName) orderData.customerName = currentUser.fullName || currentUser.username || '';
        if (!orderData.email) orderData.email = currentUser.email || '';
        if (!orderData.phoneNumber) orderData.phoneNumber = currentUser.phone || '';
      }
    }

    console.log("Submitting order data:", JSON.stringify(orderData, null, 2));

    // XỬ LÝ RESPONSE
    this.placeOrderSub = this.orderService.placeOrder(orderData)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (order: OrderResponse) => {
          console.log('Order placed successfully raw response:', JSON.stringify(order)); // Log để xem response

          const orderId = order.serialId; // Lấy ID từ response
          const guestToken = order.guestToken; // Lấy token khách

          console.log('Extracted Order ID for navigation:', orderId); // Xem giá trị ID
          console.log('Extracted Guest Token for navigation:', guestToken); // Xem giá trị token

          if (orderId && typeof orderId === 'number' && orderId > 0) {
            const paymentCode = order.paymentMethod?.toUpperCase();
            console.log("Navigating with Payment Code:", paymentCode);

            if (paymentCode === 'BANK_TRANSFER') {

              //hiện thông báo
              alert(`Đặt hàng thành công! Mã đơn hàng: #${order.orderCode ?? orderId}. Bạn cần thanh toán để mua mặt hàng trong shop chúng tôi.`);

              const queryParams = this.storageService.isLoggedIn() ? {} : { token: guestToken };
              console.log("Attempting Navigation to bank transfer. Order ID:", orderId, "Query Params:", queryParams);

              // Điều hướng
              this.router.navigate(['/bank-transfer-payment', orderId], { queryParams });
            } else {

              //hiện thông báo
              alert(`Đặt hàng thành công! Mã đơn hàng: #${order.orderCode ?? orderId}. Chúng tôi sẽ sớm liên hệ giao hàng và thu tiền tận nơi.`);

              // Cần đảm bảo route '/order-history/:id' tồn tại nếu muốn truyền id
              console.log("Navigating to order history for order ID:", orderId);
              this.router.navigate(['/order-history']);

            }
            this.cartService.loadInitialCartData(); // Cập nhật giỏ hàng 
          } else {
            console.error("Order placed but no VALID order ID (serialId > 0) received in response data. Cannot navigate.");
            this.router.navigate(['/home']); // Chuyển về home vì không có ID hợp lệ
          }
          this.errorMessage = null;
        },
        error: (err) => {
          this.errorMessage = err.message || 'Đã xảy ra lỗi khi đặt hàng.';
           // ---- PHẦN QUAN TRỌNG ĐỂ HIỂN THỊ LỖI ----
        if (err.error && typeof err.error === 'object' && err.error.message) {
          // Ưu tiên lấy message từ cấu trúc ApiResponse backend trả về
          this.errorMessage = err.error.message;
        } else if (err.message) {
          // Nếu không có cấu trúc ApiResponse, thử dùng message chung của HttpErrorResponse
          this.errorMessage = err.message;
        } else {
          // Fallback nếu không lấy được message nào
          this.errorMessage = 'Đã xảy ra lỗi không mong muốn khi đặt hàng.';
        }
      }
    });
  }


  // Xóa một item khỏi giỏ hàng
  removeItem(cartItemId: number): void {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?')) {
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    this.removeSubscription?.unsubscribe();
    this.removeSubscription = this.cartService.removeCartItem(cartItemId)
      .subscribe({
        next: () => {
          console.log('Item removed successfully:', cartItemId);
          alert('Đã xóa sản phẩm khỏi giỏ hàng.');
          // Xóa item khỏi mảng local hoặc tải lại giỏ hàng
          this.cartItems = this.cartItems.filter(item => item.cartItemId !== cartItemId);
          this.calculateTotal(); // Tính lại tổng tiền
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = `Lỗi xóa sản phẩm: ${err.message}`;
          alert(this.errorMessage);
          this.isLoading = false;
          console.error(err);
        }
      });
  }

  getProductImageUrl(imagePath: string | undefined | null): string {
    const placeholder = 'assets/images/placeholder.png';
    console.log('--- getProductImageUrl ---'); // Log bắt đầu
    console.log('Input imagePath:', imagePath); // Log giá trị đầu vào
    console.log('Using imageBaseUrl:', this.imageBaseUrl); // Log base URL đang dùng
  
    if (!imagePath || imagePath.trim() === '') {
      console.log('Returning placeholder:', placeholder); // Log khi trả về placeholder
      return placeholder;
    }
  
    // Kiểm tra xem imageBaseUrl có hợp lệ không (đề phòng)
    if (!this.imageBaseUrl) {
        console.error('imageBaseUrl is missing!');
        return placeholder; // Trả về placeholder nếu base URL bị thiếu
    }
  
    const finalUrl = this.imageBaseUrl.endsWith('/')
      ? this.imageBaseUrl + imagePath
      : this.imageBaseUrl + '/' + imagePath;
  
    console.log('Returning finalUrl:', finalUrl); // Log URL cuối cùng trả về
    return finalUrl;
  }
  // Hàm xử lý khi nhấn nút Đặt hàng
  placeOrder(): void {
    console.log('Placing order...');
    alert('Chức năng Đặt hàng chưa được triển khai!');
  }

}
