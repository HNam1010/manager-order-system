

import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StorageService } from '../services/storage.service'; // Đảm bảo đúng đường dẫn

console.log('!!! JWT CLASS INTERCEPTOR FILE LOADED !!!'); // Log load file

@Injectable()
export class JwtClassInterceptor implements HttpInterceptor {

  constructor(private storageService: StorageService) { } // Inject qua constructor

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log('--- JWT CLASS INTERCEPTOR CALLED ---');
    console.log('Intercepting request:', req.method, req.url);

    const token = this.storageService.getToken();

    const isApiAuthUrl = req.url.includes('/api/v1/auth/');
    const nonAuthUrls = [
      '/api/v1/payment-info/bank-transfer',
      // thêm các URL không cần token ở đây nếu có
    ];
    const isNonAuthUrl = nonAuthUrls.some(url => req.url.includes(url));

    let authReq = req;

    if (token != null && !isApiAuthUrl && !isNonAuthUrl) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    } else if (!token && !isApiAuthUrl && !isNonAuthUrl) {
      console.warn('>>> No token found for non-auth API URL (Class):', req.url);
    } else {
      console.log('>>> Skipping token addition for Auth or Non-Auth URL.');
    }

    console.log('--- JWT CLASS INTERCEPTOR END ---');
    return next.handle(authReq);
  }
}  