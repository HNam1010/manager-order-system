// src/app/services/payment.service.ts
import { Injectable, Inject, PLATFORM_ID } from '@angular/core'; // Thêm Inject, PLATFORM_ID nếu dùng isPlatformBrowser
import { isPlatformBrowser } from '@angular/common'; // Thêm isPlatformBrowser nếu dùng
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs'; // Thêm of nếu dùng trong handleError
import { catchError, map } from 'rxjs/operators';
// Import các model khác
import { ApiResponse } from '../models/api.model'; // Import ApiResponse
import { BankInfo } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {

    private paymentInfoApiUrl = 'http://localhost:8080/api/v1/payment-info'; // Base URL

    constructor(
        private http: HttpClient,
        @Inject(PLATFORM_ID) private platformId: Object // Inject nếu cần isPlatformBrowser trong handleError
    ) { }

    // Lấy thông tin tài khoản ngân hàng từ backend.
    getBankTransferInfo(): Observable<BankInfo> { // KIỂU TRẢ VỀ LÀ BankInfo
        const url = `${this.paymentInfoApiUrl}/bank-transfer`; // Endpoint cụ thể
        // Giả sử API backend trả về dạng ApiResponse<BankInfo>
        return this.http.get<ApiResponse<BankInfo>>(url)
            .pipe(
                map(response => {
                    if (response && response.success && response.data) {
                        return response.data; // Trả về đối tượng BankInfo
                    } else {
                        throw new Error(response?.message || 'Không thể lấy thông tin thanh toán.');
                    }
                }),
                catchError(error => this.handleError(error))
            );
    }


    /** Hàm xử lý lỗi HTTP chung */
    private handleError(error: HttpErrorResponse): Observable<never> {
        let errorMessage = 'Đã xảy ra lỗi không xác định!';
        const isBrowser = isPlatformBrowser(this.platformId); // Kiểm tra môi trường

        if (!isBrowser || error.status === 0 || error.error === null || typeof error.error === 'undefined') {
            errorMessage = `Lỗi kết nối hoặc phía Client: ${error.message || 'Không thể kết nối đến server.'}`;
            if (error.error) { console.error('[HANDLE_ERROR] Client/Network error details:', error.error); }
        } else {
            errorMessage = `Lỗi Server (Mã: ${error.status}): `;
            const errorBody = error.error;
            console.error('[HANDLE_ERROR] Raw error body from server:', errorBody);

            if (errorBody) {
                if (typeof errorBody === 'object' && errorBody.message) {
                    errorMessage += `${errorBody.message}`;
                    if (errorBody.errors && typeof errorBody.errors === 'object') {
                        console.error('[HANDLE_ERROR] Validation errors:', errorBody.errors);
                    }
                } else if (typeof errorBody === 'string') { errorMessage += errorBody; }
                else if (error.message) { errorMessage += error.message; }
                else { errorMessage += `Không có thông tin chi tiết lỗi. Status text: ${error.statusText}`; }
            } else { errorMessage += `${error.statusText || 'Unknown server error'}`; }
            console.error('Server error details object:', error);
        }
        console.error('Final error message:', errorMessage);
        return throwError(() => new Error(errorMessage));
    }
}