import { ApplicationConfig, provideZoneChangeDetection, LOCALE_ID } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideHttpClient, withFetch, HTTP_INTERCEPTORS, withInterceptorsFromDi } from '@angular/common/http'; // <-- THÊM withInterceptorsFromDi
import { registerLocaleData } from '@angular/common';
import localeVi from '@angular/common/locales/vi';
import { JwtClassInterceptor } from './core/interceptors/jwt.interceptor'; // Đảm bảo đúng đường dẫn

registerLocaleData(localeVi, 'vi');

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),

    // tích hợp đăng ký interceptor vào đây
    provideHttpClient(withInterceptorsFromDi()), // Sử dụng withInterceptorsFromDi()

    // Đăng ký interceptor class vẫn giữ nguyên
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtClassInterceptor,
      multi: true,
    },

    { provide: LOCALE_ID, useValue: 'vi' }
  ]
};