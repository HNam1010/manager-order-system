import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule, isPlatformBrowser, CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterModule, ParamMap } from '@angular/router';
import { of, Subscription, throwError, Observable } from 'rxjs'; // Import throwError
import { finalize, catchError, tap, switchMap } from 'rxjs/operators'; // Import các operators cần thiết

// Import Models và Services
import { OrderService } from '../../service/order.service';
import { PaymentService } from '../../service/payment.service'; // Lấy BankInfo từ đây hoặc models/order.model
import { OrderResponse, OrderItemResponse, BankInfo } from '../../models/order.model'; // Import các interface cần thiết
import { StorageService } from '../../core/services/storage.service';


// Import Components con
import { FooterComponent } from '../../layout/footer/footer.component';
import { HeaderComponent } from '../../layout/header/header.component';

@Component({
  selector: 'app-bank-transfer-payment',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    HeaderComponent,
    FooterComponent,
    CurrencyPipe,
    DatePipe
  ],
  templateUrl: './bank-transfer-payment.component.html',
  styleUrls: ['./bank-transfer-payment.component.scss']
})
export class BankTransferPaymentComponent implements OnInit, OnDestroy {

  // Sử dụng các kiểu dữ liệu đã import
  order: OrderResponse | null = null;
  bankInfo: BankInfo | null = null;
  isLoading: boolean = true; // Khởi tạo là true
  errorMessage: string | null = null;
  orderIdFromRoute: number | null = null; // Lưu ID lấy từ route
  transferContent: string = 'TTDH {Mã đơn hàng}'; // Nội dung chuyển khoản mặc định
  guestOrderToken: string | null = null; // Thêm biến lưu token khách

  private routeSub: Subscription | null = null;
  private orderSub: Subscription | null = null;
  private bankInfoSub: Subscription | null = null;
  private dataLoadSub: Subscription | null = null; // Chỉ cần một subscription chính

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private paymentService: PaymentService, // Đảm bảo service này tồn tại và có phương thức getBankTransferInfo
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object,
    private storageService: StorageService,
    private cdr: ChangeDetectorRef, // Inject ChangeDetectorRef
    private zone: NgZone // Inject NgZone

  ) { }

  ngOnInit(): void {
    this.isLoading = true; // Bắt đầu loading
    this.errorMessage = null;
    this.loadInitialData();
  }

  ngOnDestroy(): void {
    // Hủy subscriptions khi component bị hủy
    this.routeSub?.unsubscribe();
    this.orderSub?.unsubscribe();
    this.bankInfoSub?.unsubscribe();
  }

  loadInitialData(): void {
    this.dataLoadSub = this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        const idParam = params.get('orderid');
        if (idParam) {
          const parsedId = +idParam;
          if (!isNaN(parsedId) && parsedId > 0) {
            this.orderIdFromRoute = parsedId;
            console.log('BankTransfer: Found orderId in route:', this.orderIdFromRoute);
            return this.route.queryParamMap;
          } else {
            return throwError(() => new Error("Invalid Order ID"));
          }
        } else {
          return throwError(() => new Error("Missing Order ID"));
        }
      }),
      tap((queryParams: ParamMap) => {
        this.guestOrderToken = queryParams.get('token');
        console.log('!!! BankTransfer - Inside tap - OrderId:', this.orderIdFromRoute, 'GuestToken:', this.guestOrderToken);
      }),
      switchMap(() => {
        if (this.orderIdFromRoute) {
          return this.loadOrderDetails(this.orderIdFromRoute, this.guestOrderToken);
        } else {
          return of(null); // Chỉ trả về null nếu orderIdFromRoute không hợp lệ
        }
      }),
      // Chỉ gọi loadBankInfo nếu loadOrderDetails thành công và trả về orderData
      switchMap((orderData: OrderResponse | null) => {
        if (orderData) {
          this.order = orderData; // Gán order
          this.transferContent = this.generateTransferContent();
          console.log('BankTransfer: Order details loaded, now loading bank info...', this.order);
          return this.loadBankInfo(); // Load bank info
        }
        return of(null); // Bỏ qua load bank info nếu không có order
      }),
      catchError(err => {
        // Xử lý lỗi cuối cùng (từ paramMap, queryParamMap, loadOrderDetails, loadBankInfo)
        this.handleLoadingError(err.message || 'Lỗi không xác định khi tải dữ liệu.');
        console.error("Error in initial data loading sequence:", err);
        return of(null); // Dừng chuỗi pipe
      }),
      finalize(() => {
        this.zone.run(() => {
          this.isLoading = false;
          console.log('!!! BankTransfer - FINALIZE CALLED - isLoading:', this.isLoading);
          console.log('!!! BankTransfer - Finalize finished inside zone run');
        });

      })
    ).subscribe(bankInfo => {


      this.zone.run(() => { // Chạy cả việc gán bankInfo trong zone cho chắc
        if (bankInfo) {
          this.bankInfo = bankInfo;
          console.log('BankTransfer: Bank info assigned:', this.bankInfo);
        }
        this.isLoading = false;
        console.log('!!! BankTransfer - isLoading set to false in subscribe !!!');
      });
    });
  }


  loadOrderDetails(id: number, guestToken: string | null): Observable<OrderResponse | null> {
    let orderObservable: Observable<OrderResponse>;

    if (this.storageService?.isLoggedIn()) {
      console.log("BankTransfer: User logged in, calling getOrderByIdAndUser...");
      //CHỈ TRUYỀN orderId 
      orderObservable = this.orderService?.getOrderByIdAndUser(id);
    } else if (guestToken) {
      console.log("BankTransfer: Guest user with token, calling getGuestOrderByIdAndToken...");
      orderObservable = this.orderService?.getGuestOrderByIdAndToken(id, guestToken);
    } else {
      console.error("BankTransfer: Guest user missing order token.");
      this.handleLoadingError('Thiếu mã truy cập đơn hàng.');
      return of(null);
    }

    return orderObservable.pipe(
      catchError(err => {
        if (err.status === 401 || err.status === 403) {
          this.handleLoadingError('Bạn không có quyền xem đơn hàng này hoặc mã truy cập không hợp lệ/hết hạn.');
        } else {
          this.handleLoadingError(err?.message || 'Lỗi tải thông tin đơn hàng.');
        }
        return of(null); // Trả về null khi có lỗi
      })
    );
  }

  // Sửa hàm này để trả về Observable để chain với switchMap
  loadBankInfo(): Observable<BankInfo | null> {
    this.bankInfoSub?.unsubscribe(); // Hủy sub cũ nếu có
    return this.paymentService.getBankTransferInfo()
      .pipe(
        catchError(err => {
          console.error('BankTransfer: Error loading bank info:', err);
          // Không set errorMessage ở đây nữa, để catchError của pipe cha xử lý
          this.bankInfo = null;
          // Ném lại lỗi để catchError của pipe cha biết
          return throwError(() => new Error(err.message || 'Không thể tải thông tin thanh toán.'));
        })
      );
  }

  // Xử lý lỗi chung
  handleLoadingError(message: string): void {
    this.errorMessage = message;
    this.isLoading = false; // Dừng loading
    this.order = null;
    this.bankInfo = null;
    console.error('BankTransfer: Loading Error -', message);
  }

  // Tạo nội dung chuyển khoản
  generateTransferContent(): string {
    const prefix = this.bankInfo?.transferContentPrefix?.trim() ? this.bankInfo.transferContentPrefix.trim() + ' ' : 'TTDH ';
    // Ưu tiên orderCode, sau đó đến serialId (ID của Order), cuối cùng là ID từ route
    const identifier = this.order?.orderCode ?? this.order?.serialId ?? this.orderIdFromRoute ?? '{MaDonHang}';
    return prefix + identifier;
  }

  // Sao chép vào clipboard
  copyToClipboard(text: string | undefined | null): void {
    if (!text || !isPlatformBrowser(this.platformId)) return;
    navigator.clipboard.writeText(text).then(() => {
      alert(`Đã sao chép: ${text}`);
    }).catch(err => {
      console.error('Không thể sao chép:', err);
      alert('Lỗi: Không thể tự động sao chép.');
    });
  }

  // Về trang chủ
  goToHome(): void {
    this.router.navigate(['/home']);
  }

  // Xác nhận đã chuyển khoản
  confirmPayment(): void {
    alert('Cảm ơn bạn đã đặt hàng! Chúng tôi sẽ sớm xác nhận thanh toán và xử lý đơn hàng.');
    this.router.navigate(['/home']); // Chuyển về trang chủ
  }

  // Lấy class cho badge trạng thái (Dựa trên statusCode)
  getStatusBadgeClass(statusCode: string | undefined): string {
    if (!statusCode) return 'bg-secondary text-dark'; // Mặc định
    switch (statusCode?.toUpperCase()) { // Dùng toUpperCase để đảm bảo khớp
      case 'PENDING_CONFIRMATION': return 'text-bg-warning';
      case 'AW_BANK_TRANSFER': return 'text-bg-info'; // Màu cho chờ chuyển khoản
      case 'CONFIRMED': return 'text-bg-primary';
      case 'IN_DELIVERY': return 'text-bg-info';
      case 'COMPLETED': return 'text-bg-success';
      case 'CANCELLED': return 'text-bg-danger';
      default: return 'bg-light text-dark'; // Trạng thái không xác định
    }
  }
}