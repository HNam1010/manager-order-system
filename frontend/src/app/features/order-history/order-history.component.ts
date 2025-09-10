import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common'; // Cho *ngIf, *ngFor, date pipe, number pipe
import { RouterModule, Router } from '@angular/router'; // Cho routerLink và Router
import { Subscription } from 'rxjs';

// Import Models và Services
import { OrderService, PageableParams } from '../../service/order.service'; // Đảm bảo đúng đường dẫn
import { OrderResponse } from '../../models/order.model'; // Đảm bảo đúng đường dẫn
import { Page } from '../../models/page.model'; // Đảm bảo đúng đường dẫn

// Import Layout Components
import { HeaderComponent } from '../../layout/header/header.component';
import { FooterComponent } from '../../layout/footer/footer.component';

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule, // Cần cho routerLink
    HeaderComponent,
    FooterComponent
  ],
  templateUrl: './order-history.component.html',
  styleUrls: ['./order-history.component.scss']
})
export class OrderHistoryComponent implements OnInit, OnDestroy {

  ordersPage: Page<OrderResponse> | null = null;
  isLoading: boolean = false;
  errorMessage: string | null = null;
  private ordersSubscription: Subscription | null = null;

  // Tham số phân trang
  currentPage: number = 0;
  pageSize: number = 10; // Số đơn hàng hiển thị trên mỗi trang

  constructor(
    private orderService: OrderService,
    private router: Router // Inject Router nếu cần điều hướng khi lỗi
  ) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  ngOnDestroy(): void {
    this.ordersSubscription?.unsubscribe();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.errorMessage = null;
    const params: PageableParams = {
      page: this.currentPage,
      size: this.pageSize,
      sort: 'orderDate,desc' // Sắp xếp đơn hàng mới nhất lên đầu
    };

    this.ordersSubscription = this.orderService.getMyOrders(params).subscribe({
      next: (pageData) => {
        this.ordersPage = pageData;
        // Cập nhật lại trang hiện tại từ response phòng trường hợp request trang không hợp lệ
        this.currentPage = this.ordersPage.number;
        this.isLoading = false;
        console.log('Order history loaded:', this.ordersPage);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.message || 'Không thể tải lịch sử đơn hàng. Vui lòng thử lại.';
        console.error('Error loading order history:', err);
        // Có thể thêm điều hướng về trang chủ nếu lỗi nghiêm trọng
        if(err.status === 401 || err.status === 403) {
           this.router.navigate(['/home']);
        }
      }
    });
  }

  // Lấy tổng số trang từ ordersPage (an toàn)
  get totalPages(): number {
    return this.ordersPage?.totalPages ?? 0;
  }

  // Hàm tạo class CSS cho badge trạng thái
  getStatusBadgeClass(statusCode: string | undefined): string {
    if (!statusCode) return 'bg-secondary'; // Trạng thái không xác định
    switch (statusCode) {
      case 'PENDING_CONFIRMATION': return 'text-bg-warning'; // Bootstrap 5+ dùng text-bg-*
      case 'CONFIRMED': return 'text-bg-info';
      case 'IN_DELIVERY': return 'text-bg-primary';
      case 'COMPLETED': return 'text-bg-success';
      case 'CANCELLED': return 'text-bg-danger';
      default: return 'bg-secondary';
    }
  }

  // --- Các hàm xử lý phân trang ---
  goToPage(page: number): void {
    // Kiểm tra hợp lệ trước khi gọi API
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.loadOrders();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadOrders();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadOrders();
    }
  }
}