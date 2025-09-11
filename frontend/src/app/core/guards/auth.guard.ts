import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { StorageService } from '../services/storage.service';

export const authGuard: CanActivateFn = (route, state) => {
  const storageService = inject(StorageService);
  const router = inject(Router);

  if (storageService.isLoggedIn()) {
    return true; // Cho phép truy cập nếu đã đăng nhập
  } else {
    // Chưa đăng nhập, điều hướng về trang login
    console.log('AuthGuard: User not logged in, redirecting to /login');
    router.navigate(['/home'], { queryParams: { returnUrl: state.url } });
    return false; 
  }
};