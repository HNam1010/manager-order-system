import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

// Import các interface từ file model
import { User, Page, Role, UserUpdateRequest } from '../models/user.model'; // Import models đã sửa
import { ApiResponse } from '../models/api.model';
// import { PageableParams } from './order.service'; // Hoặc định nghĩa lại

// Định nghĩa lại PageableParams nếu chưa có
export interface PageableParams {
  page: number;
  size: number;
  sort?: string;
}

// URL API (trỏ đến Gateway)
const USER_API_URL = 'http://localhost:8080/api/v1/users'; // API quản lý Users (Admin)
const ROLE_API_URL = 'http://localhost:8080/api/v1/roles'; // API lấy Roles

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private http: HttpClient) {}

  /** Lấy danh sách người dùng (cho Admin) */
  getUsers(
    page: number,
    size: number,
    roleId: number | null = null
  ): Observable<Page<User>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'username,asc'); // Hoặc sort khác nếu muốn

    // Thêm roleId vào params nếu nó có giá trị (khác null)
    if (roleId !== null && roleId !== undefined) {
      params = params.set('roleId', roleId.toString());
    }

    // Bạn cần map nó về Page<User> nếu model User của bạn khác UserResponse
    return this.http
      .get<
        ApiResponse<{
          content: User[];
          totalElements: number;
          totalPages: number;
          number: number;
        }>
      >(USER_API_URL, { params })
      .pipe(
        map((response) => {
          if (response && response.success && response.data) {
            // Map trực tiếp nếu cấu trúc Page trả về từ backend
            return response.data as Page<User>;
          } else {
            throw new Error(
              response?.message || 'Không thể tải dữ liệu người dùng từ server.'
            );
          }
        }),
        catchError(this.handleError)
      );
  }

  getRoles(): Observable<Role[]> {
    //API trả về dạng ApiResponse<Role[]>
    return this.http.get<ApiResponse<Role[]>>(ROLE_API_URL).pipe(
      map((response) => {
        if (response && response.success && response.data) {
          return response.data;
        } else {
          console.warn(
            'No roles data received or API call failed:',
            response?.message
          );
          return []; // Trả về mảng rỗng nếu không có dữ liệu hoặc lỗi
        }
      }),
      catchError((err) => {
        console.error('Error fetching roles:', err);
        return throwError(() => new Error('Không thể tải danh sách vai trò.'));
      })
    );
  }

  /** Cập nhật User (Admin) - Cần API Backend tương ứng */
  updateUser(userId: number, request: UserUpdateRequest): Observable<User> {
    const url = `${USER_API_URL}/${userId}`;
    return this.http.put<ApiResponse<User>>(url, request).pipe(
      map((response) => {
        if (response && response.success && response.data) {
          return response.data;
        } else {
          throw new Error(response?.message || 'Lỗi cập nhật người dùng.');
        }
      }),
      catchError(this.handleError)
    );
  }

  /** Xóa User (Admin) - Cần API Backend tương ứng */
  deleteUser(userId: number): Observable<string> {
    const url = `${USER_API_URL}/${userId}`;
    return this.http.delete<ApiResponse<null>>(url).pipe(
      map((response) => {
        if (response && response.success) {
          return response.message || 'Xóa người dùng thành công.';
        } else {
          throw new Error(response?.message || 'Lỗi xóa người dùng.');
        }
      }),
      catchError(this.handleError)
    );
  }

  // Hàm xử lý lỗi HTTP chung
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Đã xảy ra lỗi khi tương tác với API User!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Lỗi Client/Mạng: ${error.error.message}`;
    } else {
      errorMessage = `Lỗi Server (Mã: ${error.status}): ${error.message}`;
      if (error.error && typeof error.error === 'string') {
        errorMessage += `\nChi tiết: ${error.error}`;
      } else if (error.error && error.error.message) {
        errorMessage += `\nChi tiết: ${error.error.message}`;
      }
      console.error('Server error body:', error.error);
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
