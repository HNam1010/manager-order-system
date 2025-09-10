import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../service/auth.service'; // Đảm bảo đúng đường dẫn
import { ResetPasswordRequest } from '../../models/auth.model'; // Tạo interface này
import { MustMatch } from '../../core/helpers/must-match.validator'; // Import custom validator
import { HeaderComponent } from '../../layout/header/header.component'; // Import Header/Footer
import { FooterComponent } from '../../layout/footer/footer.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    HeaderComponent,
    FooterComponent,
  ],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
})
export class ForgotPasswordComponent implements OnInit {
  resetForm: FormGroup;
  submitted = false;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  constructor() {
    this.resetForm = this.fb.group(
      {
        username: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],
      },
      {
        validators: MustMatch('newPassword', 'confirmPassword'), // Áp dụng validator
      }
    );
  }

  ngOnInit(): void {}

  // Getter tiện lợi để truy cập các form controls
  get f() {
    return this.resetForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = null;
    this.successMessage = null;

    // Dừng lại nếu form không hợp lệ
    if (this.resetForm.invalid) {
      console.log('Form invalid');
      return;
    }

    this.isLoading = true;

    const requestData: ResetPasswordRequest = {
      username: this.f['username'].value,
      email: this.f['email'].value,
      newPassword: this.f['newPassword'].value,
    };

    this.authService
      .resetPassword(requestData)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (response) => {
          console.log('Password reset response:', response);
          this.successMessage =
            response?.message ||
            'Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.';
          // Có thể tự động chuyển hướng sau vài giây
          setTimeout(() => this.router.navigate(['/login']), 3000);
        },
        error: (err) => {
          console.error('Password reset error:', err);
          this.errorMessage =
            err.message ||
            'Đặt lại mật khẩu thất bại. Vui lòng kiểm tra lại thông tin.';
        },
      });
  }

  togglePasswordVisibility(
    input: HTMLInputElement,
    eyeIcon: HTMLElement
  ): void {
    if (input && eyeIcon) {
      // Kiểm tra input và eyeIcon có tồn tại không
      if (input.type === 'password') {
        input.type = 'text';
        eyeIcon.classList.remove('fa-eye-slash'); // Bỏ class mắt đóng
        eyeIcon.classList.add('fa-eye'); // Thêm class mắt mở
      } else {
        input.type = 'password';
        eyeIcon.classList.remove('fa-eye'); // Bỏ class mắt mở
        eyeIcon.classList.add('fa-eye-slash'); // Thêm class mắt đóng
      }
    }
  }
}
