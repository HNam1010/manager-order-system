import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { map, take } from 'rxjs/operators';
import { UserInfo } from '../../models/auth.model';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const requiredRoles = route.data['roles'] as Array<string> | undefined; // Lấy roles từ data

    // Log thông tin đầu vào
    console.log('[RoleGuard] Activated for route:', route.routeConfig?.path);
    console.log('[RoleGuard] Required roles:', requiredRoles);

    if (!requiredRoles || requiredRoles.length === 0) {
        console.log('[RoleGuard] No specific roles required. Allowing access.');
        return true; // Cho phép nếu không yêu cầu role cụ thể
    }

    return authService.currentUser$.pipe(
        take(1), // Lấy giá trị hiện tại và hoàn thành
        map((user: UserInfo | null) => {
            console.log('[RoleGuard] Current user:', user); // Log thông tin user

            if (!user || !user?.roles || user?.roles?.length === 0) {
                console.log('[RoleGuard] No user or roles found. Redirecting to login.');
                router.navigate(['/login']); // Điều hướng đến login nếu chưa đăng nhập hoặc không có roles
                return false;
            }

            // Logic kiểm tra vai trò
            const hasRequiredRole = requiredRoles.some(requiredRole =>
                user.roles.some((userRole: string) => {
                    // So sánh không phân biệt hoa thường và kiểm tra cả tiền tố ROLE_
                    const userRoleUpper = userRole?.toUpperCase();
                    const requiredRoleUpper = requiredRole?.toUpperCase();
                    const requiredRoleWithPrefixUpper = ('ROLE_' + requiredRole).toUpperCase();
                    return userRoleUpper === requiredRoleUpper || userRoleUpper === requiredRoleWithPrefixUpper;
                })
            );

            console.log('[RoleGuard] User roles:', user.roles, 'Has required role(s) [', requiredRoles.join(', '), ']? ->', hasRequiredRole);

            if (hasRequiredRole) {
                console.log('[RoleGuard] Access GRANTED.');
                return true; // Cho phép truy cập
            } else {
                console.log('[RoleGuard] Access DENIED. Redirecting to home.');
                router.navigate(['/home']); // Điều hướng về trang chủ nếu không khớp role
                return false;
            }
        })
    );
};