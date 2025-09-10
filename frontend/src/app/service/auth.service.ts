import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, throwError, of } from 'rxjs';
import { StorageService } from '../core/services/storage.service';
import { CartService } from '../service/cart.service';
import { switchMap, map } from 'rxjs/operators';
import { ApiResponse } from '../models/api.model';
import { LoginRequest, RegisterRequest, JwtResponse, UserInfo, ResetPasswordRequest } from '../models/auth.model';
import { Router } from '@angular/router';
import { UserProfileUpdateRequest, UserResponse } from '../models/user.model';

const AUTH_API_URL = 'http://localhost:8080/api/v1/auth/';
const USER_PROFILE_API_URL = 'http://localhost:8080/api/v1/users/me';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private loggedIn = new BehaviorSubject<boolean>(false);
  private currentUser = new BehaviorSubject<UserInfo | null>(null);
  isLoggedIn$ = this.loggedIn.asObservable();
  currentUser$ = this.currentUser.asObservable();
  isAdmin$ = new BehaviorSubject<boolean>(false);

  constructor(
    private http: HttpClient,
    private storageService: StorageService,
    private router: Router,
    private cartService: CartService
  ) {
    console.log('Stored user on init:', this.storageService.getUser());
    this.loggedIn.next(this.storageService.isLoggedIn());
    this.currentUser.next(this.storageService.getUser());
    this.updateAdminStatus();
  }

  getCurrentUserProfile(): Observable<UserInfo> {
    return this.http.get<ApiResponse<UserResponse>>(USER_PROFILE_API_URL)
      .pipe(
        map(response => {
          if (response && response.success && response.data) {
            const userResponse = response.data;
            const userInfo: UserInfo = {
              id: userResponse.id,
              username: userResponse.username,
              email: userResponse.email,
              roles: userResponse.roles,
              phone: userResponse.phone,
              address: userResponse.address
            };
            return userInfo;
          } else {
            throw new Error(response?.message || 'Không thể tải thông tin người dùng.');
          }
        }),
        catchError(this.handleError)
      );
  }

  updateCurrentUserState(newUser: UserInfo): void {
    this.currentUser.next(newUser);
    this.storageService.saveUser(newUser);
    this.updateAdminStatus();
    console.log('AuthService: Current user state updated:', newUser);
  }

  updateCurrentUserProfile(updateData: UserProfileUpdateRequest): Observable<UserInfo> {
    return this.http.put<ApiResponse<UserResponse>>(USER_PROFILE_API_URL, updateData).pipe(
      map(response => {
        if (response && response.success && response.data) {
          const userResponse = response.data;
          const updatedUserInfo: UserInfo = {
            id: userResponse.id,
            username: userResponse.username,
            email: userResponse.email,
            roles: userResponse.roles
          };
          this.currentUser.next(updatedUserInfo);
          this.storageService.saveUser(updatedUserInfo);
          this.updateAdminStatus();
          return updatedUserInfo;
        } else {
          throw new Error(response?.message || 'Lỗi cập nhật thông tin từ server.');
        }
      }),
      catchError(this.handleError)
    );
  }

  login(credentials: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(AUTH_API_URL + 'login', credentials, httpOptions).pipe(
      tap(response => {
        console.log('API Login Response:', response);
        console.log('Roles:', response.roles);
        if (response?.token) {
          this.storageService.saveToken(response.token);
          const userInfo: UserInfo = {
            id: response.id,
            username: response.username,
            email: response.email,
            roles: response.roles
          };
          console.log('Saving userInfo:', userInfo);
          this.storageService.saveUser(userInfo);
          this.loggedIn.next(true);
          this.currentUser.next(userInfo);
          this.updateAdminStatus();
        } else {
          throw new Error('Login response missing token.');
        }
      }),
      switchMap(jwtResponse => {
        const guestId = this.cartService.getCurrentGuestId();
        if (guestId) {
          return this.cartService.mergeGuestCartAfterLogin().pipe(
            map(() => jwtResponse)
          );
        }
        return of(jwtResponse);
      }),
      catchError(this.handleError)
    );
  }

  register(userInfo: RegisterRequest): Observable<any> {
    return this.http.post(AUTH_API_URL + 'register', userInfo, httpOptions).pipe(
      catchError(err => {
        return throwError(() => new Error(err?.error?.message || err?.message || 'Đăng ký thất bại'));
      })
    );
  }

  logout(): void {
    console.log('Logging out user.');
    this.storageService.clean();
    this.loggedIn.next(false);
    this.currentUser.next(null);
    this.isAdmin$.next(false);
    this.router.navigate(['/login']);
  }

  resetPassword(data: ResetPasswordRequest): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(AUTH_API_URL + 'reset-password', data, httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  private hasAdminRole(): boolean {
    const user = this.storageService.getUser();
    if (!user || !user.roles) {
      console.log('No user or roles found:', user);
      return false;
    }
    const isAdmin = user.roles.some((role: string) =>
      ['ADMIN', 'ROLE_ADMIN'].includes(role.toUpperCase())
    );
    console.log('hasAdminRole result:', isAdmin, 'Roles:', user.roles);
    return isAdmin;
  }

  private updateAdminStatus(): void {
    const isAdmin = this.hasAdminRole();
    console.log('Updating admin status, isAdmin:', isAdmin);
    this.isAdmin$.next(isAdmin);
  }

  getCurrentUser(): UserInfo | null {
    return this.currentUser.getValue();
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Đã xảy ra lỗi không xác định!';
    if (error.error instanceof ErrorEvent) {
      // Lỗi client-side hoặc mạng
      errorMessage = `Lỗi Mạng/Client: ${error.error.message}`;
    } else {
      // Lỗi từ backend
      errorMessage = `Lỗi Server (Mã: ${error.status}): `;
      const errorBody = error.error; // Body lỗi từ backend
      if (errorBody && typeof errorBody === 'object' && errorBody.message) {
        // Ưu tiên lấy message từ JSON response của backend
        errorMessage = errorBody.message; // Lấy message đã được tùy chỉnh từ backend
      } else if (typeof errorBody === 'string') {
        errorMessage += errorBody;
      } else {
        errorMessage += error.message || error.statusText || 'Lỗi không rõ';
      }
      console.error('Server error details:', error); // Log chi tiết lỗi HTTP
    }
    console.error('Final error message:', errorMessage);
    // Ném về Error object với message đã được xử lý
    return throwError(() => new Error(errorMessage));
  }
}