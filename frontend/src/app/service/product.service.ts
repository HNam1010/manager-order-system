// src/app/services/product.service.ts
import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
  HttpRequest,
  HttpEvent,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import {
  Product,
  ProductType,
  ProductCreateRequest,
  ProductUpdateRequest,
} from '../models/product.model';
import { ApiResponse } from '../models/api.model';
import { Page } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private apiUrl = 'http://localhost:8080/api/v1/products';
  private apiTypeUrl = 'http://localhost:8080/api/v1/product-types';

  constructor(private http: HttpClient) {}

  getProducts(
    page: number,
    size: number,
    sort: string = 'name',
    direction: string = 'asc',
    searchTerm?: string | null,
    productTypeId?: number | null
  ): Observable<Page<Product>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sort},${direction}`);

    if (searchTerm) {
      params = params.set('search', searchTerm);
    }
    if (productTypeId !== null && productTypeId !== undefined) {
      params = params.set('typeId', productTypeId.toString());
    }

    console.log('Fetching products with params:', params.toString());

    // ---Mong đợi Page<Product> trực tiếp từ backend ---
    return this.http.get<Page<Product>>(this.apiUrl, { params: params }).pipe(
      map((pageData) => {
        // pageData bây giờ là Page<Product>
        if (pageData && Array.isArray(pageData.content)) {
          // Map ngày tháng nếu cần
          pageData.content = pageData.content.map((product) => ({
            ...product,
            createdDate: product.createdDate
              ? new Date(product.createdDate)
              : undefined,
            updateDate: product.updateDate
              ? new Date(product.updateDate)
              : undefined,
          }));
          console.log(
            `Received ${pageData.content.length} products directly as Page.`
          );
          return pageData;
        } else {
          console.error('Received invalid Page structure:', pageData);
          throw new Error('Cấu trúc dữ liệu trang sản phẩm không hợp lệ.');
        }
      }),
      catchError(this.handleError)
    );
  }

  getProductTypes(): Observable<ProductType[]> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '1000')
      .set('sort', 'name,asc');

    console.log('Fetching product types with params:', params.toString());

    // endpoint này trả về Page<ProductType> trực tiếp (KHÔNG có ApiResponse)
    return this.http
      .get<Page<ProductType>>(this.apiTypeUrl, { params }) // Mong đợi Page<ProductType>
      .pipe(
        map((pageData) => {
          // pageData là đối tượng Page<ProductType>
          if (pageData && Array.isArray(pageData.content)) {
            console.log(`Received ${pageData.content.length} product types.`);
            return pageData.content.map((item) => ({
              serialId: item.serialId,
              name: item.name,
            })) as ProductType[];
          } else {
            console.warn(
              'getProductTypes: API response structure mismatch or no content found. Expected Page<ProductType>.'
            );
            return [];
          }
        }),
        catchError((err) => {
          console.error(
            'Error loading product types (HTTP or Network Error):',
            err
          );
          return this.handleError(err);
        })
      );
  }

  /** Lấy sản phẩm theo ID - ĐÃ SỬA */
  getProductById(productId: number): Observable<Product> {
    const url = `${this.apiUrl}/${productId}`;
    // Backend trả về ApiResponse<Product>
    return this.http
      .get<ApiResponse<Product>>(url) //Mong đợi ApiResponse<Product>
      .pipe(
        map((response) => {
          //Thêm map để xử lý ApiResponse
          if (response && response.success && response.data) {
            // Map ngày tháng nếu cần
            return {
              // Trả về Product data
              ...response.data,
              createdDate: response.data.createdDate
                ? new Date(response.data.createdDate)
                : undefined,
              updateDate: response.data.updateDate
                ? new Date(response.data.updateDate)
                : undefined,
            } as Product;
          } else {
            // Ném lỗi nếu API báo lỗi hoặc cấu trúc sai
            throw new Error(
              response?.message || `Không thể lấy sản phẩm ID ${productId}.`
            );
          }
        }),
        catchError(this.handleError) // Giữ lại bắt lỗi
      );
  }

  /** Tạo sản phẩm mới */
  createProduct(
    productData: ProductCreateRequest,
    imageFile: File | null
  ): Observable<Product> {
    const formData: FormData = new FormData();
    formData.append(
      'product',
      new Blob([JSON.stringify(productData)], { type: 'application/json' })
    );
    if (imageFile) {
      formData.append('image', imageFile, imageFile.name);
    }

    // Backend trả về ApiResponse<Product>
    return this.http
      .post<ApiResponse<Product>>(this.apiUrl, formData) // <-- SỬA: Mong đợi ApiResponse<Product>
      .pipe(
        map((response) => {
          if (response && response.success && response.data) {
            // Map ngày tháng nếu cần
            return {
              // Trả về Product data đã tạo
              ...response.data,
              createdDate: response.data.createdDate
                ? new Date(response.data.createdDate)
                : undefined,
              updateDate: response.data.updateDate
                ? new Date(response.data.updateDate)
                : undefined,
            } as Product;
          } else {
            // Ném lỗi nếu API báo lỗi hoặc cấu trúc sai
            throw new Error(response?.message || 'Lỗi khi tạo sản phẩm.');
          }
        }),
        catchError(this.handleError) // Giữ lại bắt lỗi
      );
  }

  /** Cập nhật sản phẩm (Backend trả về ApiResponse<ProductResponse>) */
  updateProduct(
    productId: number,
    productData: ProductUpdateRequest,
    imageFile: File | null
  ): Observable<Product> {
    const url = `${this.apiUrl}/${productId}`;
    const formData: FormData = new FormData();
    formData.append(
      'product',
      new Blob([JSON.stringify(productData)], { type: 'application/json' })
    );
    if (imageFile) {
      formData.append('image', imageFile, imageFile.name);
    }

    // Backend trả về ApiResponse<Product>
    return this.http
      .put<ApiResponse<Product>>(url, formData) // Mong đợi ApiResponse<Product>
      .pipe(
        map((response) => {
          // Thêm map để xử lý ApiResponse
          if (response && response.success && response.data) {
            // Map ngày tháng nếu cần
            return {
              // Trả về Product data đã cập nhật
              ...response.data,
              createdDate: response.data.createdDate
                ? new Date(response.data.createdDate)
                : undefined,
              updateDate: response.data.updateDate
                ? new Date(response.data.updateDate)
                : undefined,
            } as Product;
          } else {
            // Ném lỗi nếu API báo lỗi hoặc cấu trúc sai
            throw new Error(
              response?.message || `Lỗi khi cập nhật sản phẩm ID ${productId}.`
            );
          }
        }),
        catchError(this.handleError) // Giữ lại bắt lỗi
      );
  }

  /** Xóa sản phẩm  */
  deleteProduct(productId: number): Observable<void> {
    //Kiểu trả về là Observable<void>
    const url = `${this.apiUrl}/${productId}`;
    // Backend trả về ApiResponse<Void> hoặc ApiResponse<null>
    return this.http
      .delete<ApiResponse<null>>(url) //Mong đợi ApiResponse<null> và bỏ responseType
      .pipe(
        map((response) => {
          //Thêm map để xử lý ApiResponse
          if (response && response.success) {
            console.log('Delete success message from API:', response.message); // Log thông báo thành công (tùy chọn)
            return; // Trả về void khi thành công
          } else {
            // Ném lỗi nếu API báo lỗi hoặc cấu trúc sai
            throw new Error(
              response?.message || `Lỗi khi xóa sản phẩm ID ${productId}.`
            );
          }
        }),
        catchError(this.handleError) // Giữ lại bắt lỗi
      );
  }

  /** Hàm xử lý lỗi HTTP chung - Cải thiện để ưu tiên lỗi từ map */
  private handleError(error: HttpErrorResponse | Error): Observable<never> {
    // Chấp nhận cả Error từ map
    let errorMessage = 'Đã xảy ra lỗi không xác định!';

    // Ưu tiên lấy message từ Error object (được ném từ các hàm map ở trên)
    if (error instanceof Error && !(error instanceof HttpErrorResponse)) {
      // Kiểm tra là Error nhưng không phải HttpErrorResponse
      errorMessage = error.message;
      console.error('Error caught in service (likely from map):', errorMessage);
    }
    // Nếu không phải Error object hoặc là HttpErrorResponse, xử lý như cũ
    else if (error instanceof HttpErrorResponse) {
      const httpError = error as HttpErrorResponse; // Ép kiểu để truy cập thuộc tính
      errorMessage = `Lỗi Server (Mã: ${httpError.status}): `;
      const errorBody = httpError.error; // Lỗi có thể nằm trong body
      if (errorBody) {
        // Cố gắng đọc message từ cấu trúc ApiResponse lỗi của backend
        if (typeof errorBody === 'object' && errorBody.message) {
          errorMessage += errorBody.message;
          // Hoặc đọc từ lỗi validation nếu có cấu trúc errors
        } else if (
          errorBody.errors &&
          Array.isArray(errorBody.errors) &&
          errorBody.errors.length > 0
        ) {
          const validationErrors = errorBody.errors
            .map(
              (err: any) =>
                `${err.field || 'field'}: ${err.defaultMessage || 'invalid'}`
            )
            .join('; ');
          errorMessage += `Lỗi validation - ${validationErrors}`;
          // Hoặc nếu body là string đơn giản
        } else if (typeof errorBody === 'string' && errorBody.length < 200) {
          // Tránh log cả trang HTML lỗi
          errorMessage += errorBody;
          // Fallback về message của HttpErrorResponse
        } else if (httpError.message) {
          errorMessage += httpError.message;
        } else {
          errorMessage += `${httpError.statusText || 'Unknown server error'}`;
        }
      } else {
        errorMessage += `${httpError.statusText || 'Unknown server error'}`; // Không có body lỗi
      }
      console.error('Server error details:', httpError); // Log chi tiết lỗi HTTP
    }

    console.error('Final error message being thrown:', errorMessage);
    // Luôn ném về một Error object mới theo khuyến cáo của RxJS
    return throwError(() => new Error(errorMessage));
  }
}
