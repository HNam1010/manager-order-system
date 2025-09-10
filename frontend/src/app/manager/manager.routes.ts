// manager.routes.ts
import { Routes } from '@angular/router';

export const MANAGER_ROUTES: Routes = [
  {
    path: 'managershop-home',
    loadComponent: () => import('./managershop-home/managershop.component').then(m => m.ManagershopComponent)
  },
  {
    path: 'manager-user',
    loadComponent: () => import('./manager-user/manager-user.component').then(m => m.ManagerUserComponent)
  },
  {
    path: 'management-status',
    loadComponent: () => import('./manager-status/manager-status.component').then(m => m.ManagementStatusComponent)
  },
  {
    path: 'manager-product',
    loadComponent: () => import('./manager-product/manager-product.component').then(m => m.ManagerProductComponent)
  },
  { path: '', redirectTo: 'managershop-home', pathMatch: 'full' }, // Redirect to managershop
];