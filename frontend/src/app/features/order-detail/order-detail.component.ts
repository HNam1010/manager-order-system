import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
// Thêm các Pipe và isPlatformBrowser nếu cần
import { CommonModule, isPlatformBrowser, CurrencyPipe, DatePipe } from '@angular/common';
// Thêm ActivatedRoute để lấy param, RouterModule để có thể dùng routerLink, Router để điều hướng
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subscription, finalize, catchError, throwError, Observable, of } from 'rxjs'; // Import thêm
import { OrderService } from '../../service/order.service'; // Import OrderService
import { OrderResponse, OrderItemResponse } from '../../models/order.model'; // Import các model cần thiết
import { StorageService } from '../../core/services/storage.service'; // Import nếu cần kiểm tra khách

// Import layout nếu trang chi tiết cũng dùng layout chung
import { HeaderComponent } from '../../layout/header/header.component';
import { FooterComponent } from '../../layout/footer/footer.component';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    HeaderComponent, // Thêm nếu dùng
    FooterComponent, // Thêm nếu dùng
    CurrencyPipe,     
    DatePipe          
  ],
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss']
})
export class OrderDetailComponent implements OnInit, OnDestroy {
  order: OrderResponse | null = null;
  isLoading: boolean = true;
  errorMessage: string | null = null;
  orderId: number | null = null;
  guestToken: string | null = null; // Thêm để xử lý khách

  private routeSub: Subscription | null = null;
  private orderSub: Subscription | null = null;

  // Base URL ảnh (nếu cần hiển thị ảnh sản phẩm)
  imageBaseUrl = 'http://localhost:8080/api/v1/products/images/';

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private router: Router,
    private storageService: StorageService, // Inject StorageService
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.loadOrderData();
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.orderSub?.unsubscribe();
  }

  loadOrderData(): void {
    this.routeSub = this.route.paramMap.subscribe(params => {
      const idParam = params.get('orderId');
      if (idParam) {
        const parsedId = +idParam;
        if (!isNaN(parsedId) && parsedId > 0) {
          this.orderId = parsedId;
          // Lấy guest token từ query params (nếu có)
          this.guestToken = this.route.snapshot.queryParamMap.get('token'); // Lấy token ngay lập tức
          console.log(`OrderDetail: Loading order ID: ${this.orderId}, Guest Token: ${this.guestToken}`);
          this.fetchOrderDetails(); // Gọi hàm fetch dữ liệu
        } else {
          this.handleLoadingError("ID đơn hàng không hợp lệ.");
        }
      } else {
        this.handleLoadingError("Không tìm thấy ID đơn hàng.");
      }
    });
  }

  fetchOrderDetails(): void {
    if (!this.orderId) return; // Không có ID thì không làm gì

    this.isLoading = true;
    this.errorMessage = null;
    this.orderSub?.unsubscribe(); // Hủy sub cũ

    let orderObservable: Observable<OrderResponse>;

    // Xác định cách gọi API dựa trên trạng thái đăng nhập và guest token
    if (this.storageService.isLoggedIn()) {
      console.log("OrderDetail: User logged in, calling getOrderByIdAndUser...");
      orderObservable = this.orderService.getOrderByIdAndUser(this.orderId);
    } else if (this.guestToken) {
      console.log("OrderDetail: Guest user with token, calling getGuestOrderByIdAndToken...");
      orderObservable = this.orderService.getGuestOrderByIdAndToken(this.orderId, this.guestToken);
    } else {
      console.error("OrderDetail: Access denied. No login and no guest token.");
      this.handleLoadingError('Không thể truy cập chi tiết đơn hàng.');
      return; // Dừng lại nếu không có cách xác thực
    }

    // Gọi API đã chọn
    this.orderSub = orderObservable.pipe(
      finalize(() => this.isLoading = false),
      catchError(err => {
        this.handleLoadingError(err.message || 'Lỗi tải chi tiết đơn hàng.');
        return throwError(() => err); // Ném lại lỗi
      })
    ).subscribe({
      next: (orderData) => {
        this.order = orderData;
        console.log('Order details loaded:', this.order);
      },
    });
  }

  handleLoadingError(message: string): void {
    this.errorMessage = message;
    this.isLoading = false;
    this.order = null;
    console.error('OrderDetail: Loading Error -', message);
  }

  // Hàm lấy URL ảnh (copy từ CartComponent hoặc tạo service chung)
  getProductImageUrl(imagePath: string | undefined | null): string {
    const placeholder = 'assets/images/placeholder.png'; // Đường dẫn ảnh placeholder
    if (!imagePath || imagePath.trim() === '') { return placeholder; }
    return this.imageBaseUrl + imagePath;
  }

  // Hàm lấy class cho badge trạng thái (copy từ OrderHistoryComponent)
  getStatusBadgeClass(statusCode: string | undefined): string {
    // (copy code hàm này từ OrderHistoryComponent) 
    if (!statusCode) return 'bg-secondary';
    switch (statusCode.toUpperCase()) {
      case 'PENDING_CONFIRMATION': return 'text-bg-warning';
      case 'AW_BANK_TRANSFER': return 'text-bg-info';
      case 'CONFIRMED': return 'text-bg-primary';
      case 'IN_DELIVERY': return 'text-bg-info';
      case 'COMPLETED': return 'text-bg-success';
      case 'CANCELLED': return 'text-bg-danger';
      default: return 'bg-light text-dark';
    }
  }

  // Hàm quay lại trang lịch sử (hoặc trang trước đó)
  goBack(): void {
     this.router.navigate(['/order-history']); // Quay lại trang lịch sử
  }
}