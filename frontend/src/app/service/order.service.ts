import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiResponse } from '../models/api.model';
import { Page, Pageable, Sort } from '../models/page.model'; // Sử dụng Page từ common model
import {
  OrderResponse,
  UpdateOrderStatusRequest,
  PlaceOrderRequest,
} from '../models/order.model';

// Interface cho tham số phân trang
export interface PageableParams {
  page: number;
  size: number;
  sort?: string;
  statusId?: number | null; // Thêm statusId để lọc nếu cần
}

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private apiUrl = 'http://localhost:8080/api/v1/orders'; // URL Gateway

  constructor(private http: HttpClient) {}

  placeOrder(orderData: PlaceOrderRequest): Observable<OrderResponse> {
    return this.http
      .post<ApiResponse<OrderResponse>>(this.apiUrl, orderData)
      .pipe(
        map((response) => this.extractOrderData(response)), // Dùng hàm helper để xử lý Date
        catchError(this.handleError)
      );
  }

  // Hàm lấy lịch sử đơn hàng của user hiện tại
  getMyOrders(params: PageableParams): Observable<Page<OrderResponse>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString());
    if (params.sort) {
      httpParams = httpParams.set('sort', params.sort);
    }
    // Gọi endpoint /my-orders
    return this.http
      .get<ApiResponse<Page<OrderResponse>>>(`${this.apiUrl}/my-orders`, {
        params: httpParams,
      })
      .pipe(
        map((response) => this.extractPageData(response, params.size)), // Dùng hàm helper
        catchError(this.handleError)
      );
  }

  // Hàm lấy chi tiết đơn hàng theo ID (cho user hoặc admin)
  getOrderById(orderId: number): Observable<OrderResponse> {
    const url = `${this.apiUrl}/${orderId}`;
    return this.http.get<ApiResponse<OrderResponse>>(url).pipe(
      map((response) => this.extractOrderData(response)), // Dùng hàm helper
      catchError(this.handleError)
    );
  }

  getGuestOrderByIdAndToken(
    orderId: number,
    guestToken: string
  ): Observable<OrderResponse> {
    const url = `${this.apiUrl}/${orderId}`;
    // Gửi token qua query param
    const params = new HttpParams().set('token', guestToken);
    // Không cần header Authorization vì là khách
    return this.http.get<ApiResponse<OrderResponse>>(url, { params }).pipe(
      map((response) => this.extractOrderData(response)), // Dùng helper cũ
      catchError(this.handleError)
    );
  }

  getOrderByIdAndUser(orderId: number): Observable<OrderResponse> {
    const url = `${this.apiUrl}/${orderId}`;
    console.log(
      `OrderService: Calling GET ${url} for logged-in user (token should be added by interceptor).`
    );
    // Interceptor sẽ thêm header 'Authorization'
    return this.http.get<ApiResponse<OrderResponse>>(url).pipe(
      map((response) => this.extractOrderData(response)), // Sử dụng helper để xử lý Date và lấy data
      catchError(this.handleError)
    );
  }

  // --- ADMIN METHODS ---
  // Hàm lấy TẤT CẢ đơn hàng cho Admin
  getAllOrdersForAdmin(
    params: PageableParams
  ): Observable<Page<OrderResponse>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString());
    if (params.sort) {
      httpParams = httpParams.set('sort', params.sort);
    }
    // Thêm statusId nếu có để lọc
    if (params.statusId !== null && params.statusId !== undefined) {
      httpParams = httpParams.set('statusId', params.statusId.toString());
    }
    // Gọi endpoint /admin/orders
    return this.http
      .get<ApiResponse<Page<OrderResponse>>>(`${this.apiUrl}/admin/orders`, {
        params: httpParams,
      })
      .pipe(
        map((response) => this.extractPageData(response, params.size)), // Dùng hàm helper
        catchError(this.handleError)
      );
  }

  // Hàm cập nhật trạng thái đơn hàng (cho Admin)
  updateOrderStatus(
    orderId: number,
    newStatusId: number
  ): Observable<OrderResponse> {
    const url = `${this.apiUrl}/${orderId}/status`;
    const requestBody: UpdateOrderStatusRequest = { newStatusId };
    return this.http.put<ApiResponse<OrderResponse>>(url, requestBody).pipe(
      map((response) => this.extractOrderData(response)), // Dùng hàm helper
      catchError(this.handleError)
    );
  }

  private extractOrderData(
    response: ApiResponse<OrderResponse> | null
  ): OrderResponse {
    if (response && response.success && response.data) {
      const orderData = response.data; // Dữ liệu gốc từ backend

      // Kiểm tra log xem serialId và guestToken có trong orderData không
      console.log(
        'OrderService - extractOrderData - Received raw orderData:',
        JSON.stringify(orderData)
      );

      // Trả về một đối tượng mới bao gồm TẤT CẢ các trường từ backend VÀ chuyển đổi các trường Date nếu cần
      return {
        ...orderData, // Copy tất cả các trường từ backend (serialId, orderCode, userId, guestToken, ...)
        // Chỉ ghi đè/chuyển đổi các trường Date
        orderDate: orderData.orderDate
          ? new Date(orderData.orderDate)
          : new Date(),
        updatedAt: orderData.updatedAt
          ? new Date(orderData.updatedAt)
          : undefined,
      } as OrderResponse; // Ép kiểu vẫn cần thiết nhưng giờ đối tượng đầy đủ hơn
    } else {
      // Ném lỗi cụ thể hơn
      throw new Error(
        response?.message ||
          'Dữ liệu đơn hàng không hợp lệ hoặc API không thành công.'
      );
    }
  }

  // Helper xử lý và trích xuất dữ liệu Page<OrderResponse>
  private extractPageData(
    response: ApiResponse<Page<OrderResponse>> | null,
    requestSize: number
  ): Page<OrderResponse> {
    if (
      response &&
      response.success &&
      response.data &&
      Array.isArray(response.data.content)
    ) {
      const pageDataFromBackend = response.data; // Đây là Page<OrderResponse> từ backend

      // Map lại content để chuyển đổi Date strings thành Date objects
      const mappedContent = pageDataFromBackend.content.map((order) => ({
        ...order,
        orderDate: order.orderDate ? new Date(order.orderDate) : new Date(), // Chuyển đổi
        updatedAt: order.updatedAt ? new Date(order.updatedAt) : undefined, // Chuyển đổi
        // Map date trong items nếu cần
      }));

      // Tạo đối tượng Page<OrderResponse> chuẩn cho frontend
      const frontendPage: Page<OrderResponse> = {
        content: mappedContent,
        pageable: pageDataFromBackend.pageable ?? {
          sort: { sorted: false, unsorted: true, empty: true },
          offset: pageDataFromBackend.number * requestSize,
          pageNumber: pageDataFromBackend.number,
          pageSize: requestSize,
          paged: true,
          unpaged: false,
        },
        sort: pageDataFromBackend.sort ?? {
          sorted: false,
          unsorted: true,
          empty: true,
        },
        // Gán các trường còn lại từ pageDataFromBackend
        last: pageDataFromBackend.last,
        totalPages: pageDataFromBackend.totalPages,
        totalElements: pageDataFromBackend.totalElements,
        size: pageDataFromBackend.size ?? requestSize, // Ưu tiên size từ backend
        number: pageDataFromBackend.number,
        first: pageDataFromBackend.first,
        numberOfElements: pageDataFromBackend.numberOfElements,
        empty: pageDataFromBackend.empty,
      };

      // Kiểm tra và thêm giá trị mặc định nếu pageable/sort thiếu (phòng trường hợp backend không trả về)
      if (!frontendPage.pageable) {
        const defaultSort: Sort = {
          sorted: false,
          unsorted: true,
          empty: true,
        }; // Mặc định không sort
        frontendPage.pageable = {
          sort: defaultSort,
          offset: frontendPage.number * frontendPage.size,
          pageNumber: frontendPage.number,
          pageSize: frontendPage.size,
          paged: true,
          unpaged: false,
        };
      }
      if (!frontendPage.sort) {
        frontendPage.sort = { sorted: false, unsorted: true, empty: true }; // Mặc định không sort
      }

      return frontendPage;
    } else {
      console.warn(
        'extractPageData: API response structure mismatch or data is missing or content is not an array.'
      );
      // Trả về Page rỗng chuẩn theo interface frontend
      const defaultSort: Sort = { sorted: false, unsorted: true, empty: true };
      const defaultPageable: Pageable = {
        sort: defaultSort,
        offset: 0,
        pageNumber: 0,
        pageSize: requestSize,
        paged: true,
        unpaged: false,
      };
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: requestSize,
        first: true,
        last: true,
        numberOfElements: 0,
        empty: true,
        pageable: defaultPageable,
        sort: defaultSort,
      } as Page<OrderResponse>;
    }
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    // Log lỗi gốc để debug (giữ lại)
    console.error('Server error details:', error);

    let displayMessage = 'Đã xảy ra lỗi không mong muốn.'; // Thông báo mặc định

    if (error.status === 0) {
      // Lỗi client-side hoặc network.
      displayMessage =
        'Lỗi mạng hoặc không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối.';
    } else if (
      error.error &&
      typeof error.error === 'object' &&
      error.error.message
    ) {
      // Backend đã trả về lỗi có cấu trúc ApiResponse
      displayMessage = error.error.message; // <--- LẤY MESSAGE TỪ BACKEND Ở ĐÂY
    } else {
      // Nếu backend không trả về cấu trúc mong muốn hoặc lỗi khác
      displayMessage = `Lỗi từ máy chủ (Mã: ${error.status}). Vui lòng thử lại sau.`;
    }

    // Log message cuối cùng sẽ được throw (giữ lại để debug)
    console.error('Final error message being thrown:', displayMessage);

    // Throw một Error object chứa message lỗi ĐÃ ĐƯỢC XỬ LÝ
    return throwError(() => new Error(displayMessage));
  }
}
