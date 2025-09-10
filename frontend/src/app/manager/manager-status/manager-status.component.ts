import {
  Component,
  OnInit,
  OnDestroy,
  Inject,
  PLATFORM_ID,
  TemplateRef,
} from '@angular/core';
import {
  CommonModule,
  isPlatformBrowser,
  CurrencyPipe,
  DecimalPipe,
} from '@angular/common'; // Import isPlatformBrowser
import { FormsModule, ReactiveFormsModule } from '@angular/forms'; // Thêm FormsModule cho [(ngModel)]
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators'; // Import finalize

import { OrderResponse, Page } from '../../models/order.model'; // Chỉ cần OrderResponse và Page ở đây nếu UpdateOrderStatusRequest không dùng trực tiếp
import { OrderService, PageableParams } from '../../service/order.service'; // Import service và PageableParams

// Import các component con (NÊN đặt ở layout cha)
import { ManagerSidebarComponent } from '../layout/manager-sidebar/manager-sidebar.component';
import { FooterComponent } from '../../layout/footer/footer.component';

// Định nghĩa interface cho trạng thái để rõ ràng hơn
interface OrderStatus {
  id: number;
  name: string;
  code: string;
}

@Component({
  selector: 'app-order-management', // Đảm bảo selector đúng với component của bạn
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    FooterComponent,
    ManagerSidebarComponent,
  ],
  templateUrl: './manager-status.component.html', // Đảm bảo tên file đúng
  styleUrls: ['./manager-status.component.scss'], // Đảm bảo tên file đúng
})
export class ManagementStatusComponent implements OnInit, OnDestroy {
  orders: OrderResponse[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  // QUAN TRỌNG: Nên tải danh sách này từ backend
  availableStatuses: OrderStatus[] = [
    { id: 1, name: 'Chờ Xác Nhận', code: 'PENDING_CONFIRMATION' },
    { id: 2, name: 'Đã Xác Nhận', code: 'CONFIRMED' },
    { id: 3, name: 'Đang Giao', code: 'IN_DELIVERY' },
    { id: 4, name: 'Hoàn Thành', code: 'COMPLETED' },
    { id: 5, name: 'Đã Hủy', code: 'CANCELLED' },
    { id: 6, name: 'Chờ Chuyển Khoản', code: 'AW_BANK_TRANSFER' },
  ];

  selectedStatusFilter: number | null = null; // Lọc theo ID trạng thái

  editingOrder: OrderResponse | null = null;
  selectedNewStatusId: number | null = null; // ID trạng thái mới sẽ gửi đi
  modalErrorMessage: string | null = null;
  isSaving: boolean = false; // Cờ cho nút Lưu trong modal

  private currentModalRef: NgbModalRef | null = null;
  private ordersSubscription: Subscription | null = null;
  private modalSubscription: Subscription | null = null; // Subscription cho việc cập nhật status

  constructor(
    private orderService: OrderService,
    private modalService: NgbModal,
    @Inject(PLATFORM_ID) private platformId: Object // Inject platformId để kiểm tra môi trường browser
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  ngOnDestroy(): void {
    // Hủy các subscription để tránh memory leak
    this.ordersSubscription?.unsubscribe();
    this.modalSubscription?.unsubscribe();
    this.currentModalRef?.close(); // Đóng modal nếu còn mở khi component bị hủy
  }

  loadOrders(): void {
    this.isLoading = true;
    this.errorMessage = null;
    const params: PageableParams = {
      page: this.currentPage,
      size: this.pageSize,
      sort: 'orderDate,desc', // Sắp xếp theo ngày đặt hàng giảm dần
      statusId: this.selectedStatusFilter, // Lọc theo statusId nếu có
    };

    // Gọi API lấy danh sách đơn hàng cho admin
    this.ordersSubscription = this.orderService
      .getAllOrdersForAdmin(params)
      .pipe(finalize(() => (this.isLoading = false))) // Đảm bảo isLoading = false dù thành công hay lỗi
      .subscribe({
        next: (pageData: Page<OrderResponse>) => {
          // Sử dụng Page<OrderResponse>
          this.orders = pageData.content;
          this.totalPages = pageData.totalPages;
          this.totalElements = pageData.totalElements;
          this.currentPage = pageData.number; // currentPage là index (bắt đầu từ 0)
          console.log('Orders loaded:', this.orders);
        },
        error: (err) => {
          // Lấy thông điệp lỗi chi tiết hơn nếu có từ backend
          this.errorMessage =
            err?.error?.message ||
            err?.message ||
            'Không thể tải danh sách đơn hàng.';
          console.error('Error loading orders:', err);
          this.orders = []; // Xóa danh sách cũ khi có lỗi
        },
      });
  }

  // Xử lý khi thay đổi bộ lọc trạng thái
  onStatusFilterChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const statusIdString = selectElement.value; // Lấy giá trị dạng string từ select

    //SỬA LẠI LOGIC CHUYỂN ĐỔI VÀ GÁN
    if (statusIdString && statusIdString !== 'null') {
      const parsedId = parseInt(statusIdString, 10); // Thử parse sang số
      // Chỉ gán nếu parse thành công và không phải NaN
      if (!isNaN(parsedId)) {
        this.selectedStatusFilter = parsedId;
        console.log('Status filter changed to ID:', this.selectedStatusFilter);
      } else {
        // Nếu parse lỗi (NaN) hoặc giá trị không hợp lệ, coi như chọn "Tất cả"
        console.warn(
          'Invalid status ID string received:',
          statusIdString,
          '- Resetting filter.'
        );
        this.selectedStatusFilter = null;
      }
    } else {
      // Nếu giá trị là "null" (chuỗi) hoặc rỗng, chọn "Tất cả"
      this.selectedStatusFilter = null;
      console.log('Status filter reset to all.');
    }

    this.currentPage = 0; // Reset về trang đầu tiên khi đổi filter
    this.loadOrders(); // Tải lại danh sách đơn hàng với filter mới (hoặc không filter)
  }

  // Hàm lấy các trạng thái hợp lệ tiếp theo dựa trên STATUS CODE hiện tại
  getValidNextStatuses(currentStatusCode: string | undefined): OrderStatus[] {
    // console.log("Getting valid next statuses for code:", currentStatusCode);
    if (!currentStatusCode) return [];

    const currentStatus = this.availableStatuses.find(
      (s) => s.code === currentStatusCode
    );
    if (
      !currentStatus ||
      currentStatusCode === 'COMPLETED' ||
      currentStatusCode === 'CANCELLED'
    ) {
      return []; // Không thể thay đổi từ COMPLETED hoặc CANCELLED
    }

    // Logic xác định trạng thái tiếp theo (CẦN ĐIỀU CHỈNH THEO WORKFLOW THỰC TẾ)
    let nextPossibleCodes: string[] = [];
    switch (currentStatusCode) {
      case 'AW_BANK_TRANSFER': // Từ chờ chuyển khoản
        nextPossibleCodes = ['PENDING_CONFIRMATION', 'CONFIRMED', 'CANCELLED']; // Có thể xác nhận hoặc hủy
        break;
      case 'PENDING_CONFIRMATION': // Từ chờ xác nhận
        nextPossibleCodes = ['CONFIRMED', 'CANCELLED'];
        break;
      case 'CONFIRMED': // Từ đã xác nhận
        nextPossibleCodes = ['IN_DELIVERY', 'CANCELLED']; // Có thể hủy trước khi giao? (Tùy nghiệp vụ)
        break;
      case 'IN_DELIVERY': // Từ đang giao
        nextPossibleCodes = ['COMPLETED', 'CANCELLED'];
        break;

      default:
        nextPossibleCodes = []; // Mặc định không cho chuyển từ trạng thái không xác định
    }

    // Lọc ra các đối tượng OrderStatus tương ứng
    return this.availableStatuses.filter((status) =>
      nextPossibleCodes.includes(status.code)
    );
  }

  // Kiểm tra xem có thể mở modal để cập nhật trạng thái không
  canUpdateStatus(currentStatusCode: string | undefined): boolean {
    // Cho phép cập nhật nếu trạng thái hiện tại không phải là COMPLETED hoặc CANCELLED
    return (
      !!currentStatusCode &&
      currentStatusCode !== 'COMPLETED' &&
      currentStatusCode !== 'CANCELLED'
    );
  }

  // Mở modal cập nhật trạng thái
  openUpdateStatusModal(content: TemplateRef<any>, order: OrderResponse): void {
    if (!this.canUpdateStatus(order.statusCode)) {
      console.warn(
        `Trạng thái đơn hàng ${order.serialId} (${order.statusDescription}) không thể thay đổi.`
      );
      alert(
        `Trạng thái đơn hàng "${order.statusDescription}" không thể thay đổi.`
      ); // Thông báo cho người dùng
      return;
    }
    // Reset trạng thái modal trước khi mở
    this.editingOrder = { ...order }; // Tạo bản sao để tránh thay đổi trực tiếp
    this.selectedNewStatusId = null; // Reset trạng thái mới được chọn
    this.modalErrorMessage = null; // Xóa thông báo lỗi cũ
    this.isSaving = false; // Reset trạng thái nút lưu

    // Mở modal
    this.currentModalRef = this.modalService.open(content, {
      ariaLabelledBy: 'modal-basic-title',
      centered: true,
    });

    // Xử lý khi modal đóng (dù thành công hay bị hủy)
    this.currentModalRef.result.then(
      (result) => {
        console.log(`Modal closed with: ${result}`);
        this.resetUpdateStatusModalState(); // Dọn dẹp state khi modal đóng
      },
      (reason) => {
        console.log(`Modal dismissed with: ${reason}`);
        this.resetUpdateStatusModalState(); // Dọn dẹp state khi modal bị dismiss
      }
    );
  }

  // Đóng modal đang mở
  closeUpdateStatusModal(reason: string = 'Cancel click'): void {
    this.currentModalRef?.dismiss(reason);
  }

  // Reset state của modal
  resetUpdateStatusModalState(): void {
    this.editingOrder = null;
    this.selectedNewStatusId = null;
    this.modalErrorMessage = null;
    this.isSaving = false;
    this.currentModalRef = null; // Quan trọng: Đặt lại ref modal
  }

  // Hàm hiển thị PTTT
  getPaymentMethodDisplay(methodCode: string | undefined): string {
    if (!methodCode) return 'N/A';
    switch (methodCode.toUpperCase()) {
      case 'COD':
        return 'Thanh toán khi nhận hàng';
      case 'BANK_TRANSFER':
        return 'Chuyển khoản';
      default:
        return methodCode;
    }
  }

  // Thực thi việc gọi API cập nhật trạng thái
  executeUpdateStatus(): void {
    // Kiểm tra điều kiện cần thiết
    if (this.selectedNewStatusId === null || !this.editingOrder) {
      this.modalErrorMessage = 'Vui lòng chọn trạng thái mới.';
      return;
    }

    this.isSaving = true; // Bắt đầu lưu
    this.modalErrorMessage = null; // Xóa lỗi cũ

    // Gọi service để cập nhật, gửi ID đơn hàng và ID trạng thái mới
    this.modalSubscription = this.orderService
      .updateOrderStatus(this.editingOrder.serialId, this.selectedNewStatusId)
      .pipe(finalize(() => (this.isSaving = false))) // Đảm bảo isSaving = false sau khi hoàn thành
      .subscribe({
        next: (updatedOrder) => {
          console.log('Order status updated successfully:', updatedOrder);
          alert(
            `Đã cập nhật trạng thái cho đơn hàng #${this.editingOrder?.serialId} thành công.`
          );
          this.currentModalRef?.close('Status Updated'); // Đóng modal thành công
          this.loadOrders(); // Tải lại danh sách đơn hàng để cập nhật giao diện
        },
        error: (err) => {
          // Hiển thị lỗi cụ thể từ backend nếu có
          this.modalErrorMessage =
            err?.error?.message ||
            err?.message ||
            'Lỗi cập nhật trạng thái đơn hàng.';
          console.error('Error updating order status:', err);
        },
      });
  }

  // Các hàm phân trang (Giữ nguyên)
  goToPage(page: number): void {
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

  // Thêm getter này nếu template phân trang cần (Giữ nguyên)
  get totalPagesGetter(): number {
    return this.totalPages;
  }

  // Hàm lấy class cho badge trạng thái (Thêm case cho AW_BANK_TRANSFER)
  getStatusBadgeClass(statusCode: string | undefined): string {
    if (!statusCode) return 'bg-secondary'; // Mặc định nếu không có status
    switch (statusCode) {
      case 'PENDING_CONFIRMATION':
        return 'text-bg-warning';
      case 'AW_BANK_TRANSFER':
        return 'text-bg-secondary'; // Màu cho chờ chuyển khoản
      case 'CONFIRMED':
        return 'text-bg-info';
      case 'IN_DELIVERY':
        return 'text-bg-primary';
      case 'COMPLETED':
        return 'text-bg-success';
      case 'CANCELLED':
        return 'text-bg-danger';
      default:
        return 'bg-light text-dark'; // Trạng thái không xác định
    }
  }
}
