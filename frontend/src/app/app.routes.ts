import { RouterModule, Routes } from '@angular/router';
import { OrderHistoryComponent } from './features/order-history/order-history.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' }, // mặc định trả về trang mặc định
  {
    path: 'home',
    loadComponent: () =>
      import('./features/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'cart',
    loadComponent: () =>
      import('./features/cart/cart.component').then((m) => m.CartComponent),
  },
  {
    path: 'bank-transfer-payment/:orderid', //theo id
    loadComponent: () =>
      import(
        './features/bank-transfer-payment/bank-transfer-payment.component'
      ).then((m) => m.BankTransferPaymentComponent),
  },
  {
    path: 'product/:id', //theo id
    loadComponent: () =>
      import('./features/product-detail/product-detail.component').then(
        (m) => m.ProductDetailComponent
      ),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./auth/register/register.component').then(
        (m) => m.RegisterComponent
      ),
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/forgot-password/forgot-password.component').then(
        (m) => m.ForgotPasswordComponent
      ),
  },
  {
    path: 'inforshop',
    loadComponent: () =>
      import('./features/inforshop/inforshop.component').then(
        (m) => m.InforshopComponent
      ),
  },
  {
    path: 'order-history',
    loadComponent: () =>
      import('./features/order-history/order-history.component').then(
        (m) => m.OrderHistoryComponent
      ),
    canActivate: [authGuard], // Yêu cầu đăng nhập
    //data: { roles: ['CUSTOMER', 'ADMIN'] } // Cho phép cả CUSTOMER và ADMIN
  },
  {
    path: 'order-details/:orderId', // Đường dẫn với tham số orderId
    loadComponent: () =>
      import('./features/order-detail/order-detail.component').then(
        (m) => m.OrderDetailComponent
      ),
    canActivate: [authGuard], //Chỉ cần đăng nhập là có thể xem (Guard sẽ check quyền cụ thể hơn nếu cần)
  },
  {
    path: 'manager',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN'] },
    loadChildren: () =>
      import('./manager/manager.routes').then((m) => m.MANAGER_ROUTES),
  },

  {
    path: 'user-profile',
    loadComponent: () =>
      import('./features/profile/user-profile.component').then(
        (m) => m.UserProfileComponent
      ),
    canActivate: [authGuard, roleGuard],
  },
  { path: '**', redirectTo: '/home' }, // Chuyển hướng nếu không tìm thấy đường dẫn
];

export const AppRoutingModule = RouterModule.forRoot(routes);
