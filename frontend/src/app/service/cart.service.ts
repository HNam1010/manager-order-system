import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import {
  HttpClient,
  HttpHeaders,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { CartItem, AddToCartRequest } from '../models/cart.model';
import { ApiResponse } from '../models/api.model';
import { StorageService } from '../core/services/storage.service';
import { jwtDecode } from 'jwt-decode'; // <-- Import jwt_decode

const CART_API_URL = 'http://localhost:8080/api/v1/cart';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  private cartItemCountSubject = new BehaviorSubject<number>(0);
  cartItemCount$ = this.cartItemCountSubject.asObservable();
  private guestCartId: string | null = null;
  private readonly GUEST_CART_ID_KEY = 'guest-cart-id';
  private isBrowser: boolean;

  //BehaviorSubject để quản lý số lượng item
  private cartItemsSource = new BehaviorSubject<CartItem[]>([]);
  cartItems$ = this.cartItemsSource.asObservable();

  //Observable cho TỔNG SỐ LƯỢNG
  private totalItemQuantitySubject = new BehaviorSubject<number>(0);
  totalItemQuantity$ = this.totalItemQuantitySubject.asObservable(); // Observable cho tổng số lượng (nếu cần ở đâu đó)

  // THÊM Observable cho SỐ LOẠI MẶT HÀNG
  private distinctItemCountSubject = new BehaviorSubject<number>(0);

  //Observable emitting the count of distinct item types in the cart.
  distinctItemCount$ = this.distinctItemCountSubject.asObservable(); // Header sẽ dùng cái này

  constructor(
    private http: HttpClient,
    private storageService: StorageService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);

    if (this.isBrowser) {
      this.guestCartId = localStorage.getItem(this.GUEST_CART_ID_KEY);
      this.loadInitialCartData(); // Load giỏ hàng ban đầu
    } else {
      console.log(
        'CartService: Not running in browser, localStorage access skipped initially.'
      );
    }
  }

  // ---  HÀM TẢI DỮ LIỆU BAN ĐẦU ---
  loadInitialCartData(): void {
    if (!this.isBrowser) return;
    this.getCartItems().subscribe({
      error: (err) => console.error('Failed initial cart load:', err),
    });
  }

  public getCurrentGuestId(): string | null {
    return this.getGuestCartId();
  }

  private createHeaders(): HttpHeaders {
    const headersConfig: { [key: string]: string } = {
      'Content-Type': 'application/json',
    };

    if (this.isBrowser) {
      if (this.storageService.isLoggedIn()) {
        const token = this.storageService.getToken();
        if (token) {
          try {
            const decodedToken: any = jwtDecode(token);
            const userId = decodedToken.userId; // Đảm bảo claim name là 'userId'

            if (userId) {
              headersConfig['X-User-ID'] = userId.toString();
            }
          } catch (error) {
            console.error('Error decoding JWT:', error);
            this.storageService.removeToken(); // Xóa token và user nếu lỗi
          }
        }
      } else {
        const guestId = this.getCurrentGuestId(); // Lấy hoặc tạo guestId
        if (guestId) {
          headersConfig['X-Guest-ID'] = guestId.toString();
        }
      }
    }

    return new HttpHeaders(headersConfig);
  }

  private getGuestCartId(): string | null {
    if (!this.isBrowser) return null;

    if (!this.guestCartId) {
      const storedId = localStorage.getItem(this.GUEST_CART_ID_KEY);
      if (storedId) {
        this.guestCartId = storedId;
      } else {
        this.guestCartId = crypto.randomUUID();
        localStorage.setItem(this.GUEST_CART_ID_KEY, this.guestCartId);
        console.log('Generated new Guest Cart ID:', this.guestCartId);
      }
    }

    return this.guestCartId;
  }

  private clearGuestCartId(): void {
    if (this.isBrowser) {
      localStorage.removeItem(this.GUEST_CART_ID_KEY);
    }
    this.guestCartId = null;
  }

  getCartItems(): Observable<CartItem[]> {
    if (!this.isBrowser) return of([]);
    const headers = this.createHeaders();

    return this.http
      .get<ApiResponse<CartItem[]>>(CART_API_URL, { headers })
      .pipe(
        map((response) =>
          response?.success && Array.isArray(response.data) ? response.data : []
        ),

        tap((items) => {
          this.updateCartState(items);
        }),
        catchError((err) => {
          console.error('Error fetching cart items:', err);
          this.updateCartState([]); // Reset state khi lỗi
          return throwError(() => new Error('Không thể tải giỏ hàng.'));
        })
      );
  }

  addToCart(productId: number, quantity: number): Observable<CartItem> {
    if (!this.isBrowser)
      return throwError(() => new Error('Cannot add to cart outside browser'));
    const headers = this.createHeaders();
    const body = { productId: productId, quantity: quantity };
    console.log('addToCart headers:', headers); // Log headers
    return this.http
      .post<ApiResponse<CartItem>>(CART_API_URL, body, { headers })
      .pipe(
        map((response) => {
          if (response && response.success && response.data) {
            return response.data;
          } else {
            throw new Error(response?.message || 'Lỗi thêm vào giỏ hàng.');
          }
        }),
        tap(() => {
          console.log('Item added, refreshing cart state...');
          this.loadInitialCartData();
        }), // <-- Gọi load lại
        catchError(this.handleError)
      );
  }

  updateCartItem(cartItemId: number, quantity: number): Observable<CartItem> {
    const headers = this.createHeaders();
    const body = { quantity };
    return this.http
      .put<ApiResponse<CartItem>>(`${CART_API_URL}/${cartItemId}`, body, {
        headers,
      })
      .pipe(
        map((response) => {
          if (response && response.success && response.data) {
            return response.data;
          } else {
            throw new Error(response?.message || 'Lỗi cập nhật giỏ hàng.');
          }
        }),
        tap(() => {
          console.log('Item updated, refreshing cart state...');
          this.loadInitialCartData();
        }), // <-- Gọi load lại
        catchError(this.handleError)
      );
  }

  removeCartItem(cartItemId: number): Observable<void> {
    if (!this.isBrowser)
      return throwError(() => new Error('Cannot remove item outside browser'));
    const headers = this.createHeaders();

    return this.http
      .delete<ApiResponse<void>>(`${CART_API_URL}/${cartItemId}`, { headers })
      .pipe(
        map((response) => {
          if (response && response.success) {
            // this.fetchCartItemCount();
            return;
          } else {
            throw new Error(
              response?.message || 'Lỗi xóa sản phẩm khỏi giỏ hàng.'
            );
          }
        }),
        tap(() => {
          console.log('Item removed, refreshing cart state...');
          this.loadInitialCartData();
        }), // <-- Gọi load lại
        catchError(this.handleError)
      );
  }

  mergeGuestCartAfterLogin(): Observable<void> {
    const guestId = this.getCurrentGuestId(); // Lấy guestId hiện có
    if (!guestId || !this.storageService.isLoggedIn()) {
      console.log('mergeGuestCartAfterLogin: No guest ID or not logged in.');
      return of(undefined);
    }

    console.log(
      'mergeGuestCartAfterLogin: Attempting to merge guest cart ID:',
      guestId
    );
    // const headers = new HttpHeaders({ 'X-Guest-ID': guestId });

    const headers = this.createHeaders().set('X-Guest-ID', guestId); // Thêm X-Guest-ID vào đây nữa

    return this.http
      .post<ApiResponse<void>>(
        `http://localhost:8080/api/v1/cart/merge`,
        {},
        { headers }
      )
      .pipe(
        tap((response) => {
          if (response && response.success) {
            console.log('Guest cart merged successfully.');
            this.clearGuestCartId(); // Chỉ xóa guestId local khi merge thành công
            this.loadInitialCartData(); // Tải lại giỏ hàng đã merge
          } else {
            console.warn('Failed to merge guest cart:', response?.message);
          }
        }),
        map((response) => {
          undefined;
        }),
        catchError((err) => {
          console.error('Error merging guest cart:', err);
          return of(undefined);
        })
      );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Đã xảy ra lỗi không xác định!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Lỗi Client/Mạng: ${error.error.message}`;
    } else {
      errorMessage = `Lỗi Server (Mã: ${error.status}): `;
      let errorBodyString =
        typeof error.error === 'string'
          ? error.error
          : JSON.stringify(error.error);
      errorMessage +=
        errorBodyString ||
        error.message ||
        error.statusText ||
        'Lỗi không xác định';
    }
    console.error('CartService Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }

  clearLocalCartAndUpdateCount(): void {
    // Gọi hàm cập nhật state với mảng rỗng sẽ tự động set count về 0
    this.updateCartState([]);
    // this.clearGuestCartId(); // Có thể gọi nếu cần
    console.log('CartService: Local cart state cleared.');
  }

  // HÀM HELPER MỚI ĐỂ CẬP NHẬT CÁC SUBJECT
  private updateCartState(items: CartItem[]): void {
    const totalQuantity = items.reduce(
      (sum, item) => sum + (item.quantity || 0),
      0
    );
    const distinctCount = items.length; // Số loại SP = số lượng phần tử trong mảng items trả về

    this.cartItemsSource.next([...items]); // Cập nhật danh sách
    this.totalItemQuantitySubject.next(totalQuantity); // Cập nhật TỔNG SỐ LƯỢNG
    this.distinctItemCountSubject.next(distinctCount); // Cập nhật SỐ LOẠI SẢN PHẨM

    console.log(
      'CartService: State updated - Distinct Count:',
      distinctCount,
      'Total Quantity:',
      totalQuantity
    );
  }
}
