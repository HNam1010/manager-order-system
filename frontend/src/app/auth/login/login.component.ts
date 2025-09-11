import { Component, OnInit, OnDestroy } from '@angular/core';
// THÊM: CommonModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
// THÊM: Router, Subscription
import { Router, RouterLink, RouterModule } from '@angular/router'; // Giữ RouterLink, thêm Router
import { Subscription } from 'rxjs';

// Import các service cần thiết
import { AuthService } from '../../service/auth.service';
import { StorageService } from '../../core/services/storage.service';

// Import layout components
import { FooterComponent } from '../../layout/footer/footer.component';
import { HeaderComponent } from '../../layout/header/header.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,         
    ReactiveFormsModule,  
    RouterLink,           
    RouterModule,
    HeaderComponent,
    FooterComponent
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
// THÊM: implements OnInit, OnDestroy
export class LoginComponent implements OnInit, OnDestroy {

  loginForm: FormGroup;
  isLoggedIn = false;
  isLoginFailed = false;
  isLoading = false;

  // biến đếm ngược thời gian
  isRetrying = false; // Cờ báo đang trong quá trình đếm ngược chờ thử lại
  retryCountdown = 10; // Số giây đếm ngược ban đầu
  private countdownIntervalId: any = null; // ID của interval để có thể clear
  retryErrorMessage: string | null = null; // Thông báo đếm ngược riêng

  errorMessage: string | null = null; //  Biến lưu trữ thông báo lỗi


  private loginSubscription: Subscription | null = null;

  // THÊM CONSTRUCTOR VỚI DEPENDENCY INJECTION
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private storageService: StorageService,
    private router: Router
  ) {
    // Khởi tạo form
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
     });
  }

  // THÊM ngOnInit VÀ ngOnDestroy
  ngOnInit(): void {
    // Nếu đã đăng nhập, chuyển hướng
    if (this.storageService.isLoggedIn()) {
      console.log('User already logged in, redirecting to home.');
      this.router.navigate(['/home']);
    }
  }

  ngOnDestroy(): void {
    this.loginSubscription?.unsubscribe();

    if (this.countdownIntervalId) {
      clearInterval(this.countdownIntervalId);
    }
  }

  // THÊM PHƯƠNG THỨC onSubmit
  onSubmit(): void {
    // Reset trạng thái retry và lỗi trước mỗi lần submit mới
    this.errorMessage = null;
    this.isLoginFailed = false;
    this.stopRetryCountdown(); // Dừng countdown cũ nếu đang chạy

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const { username, password } = this.loginForm.value;

    this.loginSubscription = this.authService.login({ username, password }).subscribe({
      next: data => {
        this.isLoading = false;
        this.isLoggedIn = true;
        this.isLoginFailed = false;
        alert('Đăng nhập thành công!');
        this.router.navigate(['/home']);
      },
      error: err => {
        this.isLoading = false;
        // Gán lỗi chính
        this.errorMessage = err.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại!';
        this.isLoginFailed = true;
        // BẮT ĐẦU ĐẾM NGƯỢC KHI LỖI
        this.startRetryCountdown();
      }
    });
  }

  // THÊM CÁC HÀM CHO ĐẾM NGƯỢC
  startRetryCountdown(): void {
    this.isRetrying = true; // Bật trạng thái chờ retry
    this.retryCountdown = 5; // Đặt lại số giây
    this.retryErrorMessage = `Có lỗi!!!`; // Thông báo ban đầu

    // Clear interval cũ nếu có (phòng trường hợp click submit liên tục)
    if (this.countdownIntervalId) {
      clearInterval(this.countdownIntervalId);
    }

    this.countdownIntervalId = setInterval(() => {
      this.retryCountdown--; // Giảm số giây
      if (this.retryCountdown > 0) {
        this.retryErrorMessage = `Có lỗi!!! Hệ thống đang kiểm tra vui lòng đợi ${this.retryCountdown} giây để thử lại...`;
      } else {
        // Đếm ngược kết thúc
        this.stopRetryCountdown();
        // Reset form
        this.loginForm.controls['username'].reset();
        this.loginForm.controls['password'].reset();
      }
    }, 1000); // Chạy mỗi giây
  }

  stopRetryCountdown(): void {
    if (this.countdownIntervalId) {
      clearInterval(this.countdownIntervalId); // Dừng interval
      this.countdownIntervalId = null;
    }
    this.isRetrying = false; // Tắt trạng thái chờ retry
    this.retryErrorMessage = null; // Xóa thông báo đếm ngược
  }



  togglePasswordVisibility(input: HTMLInputElement, eyeIcon: HTMLElement) {
    if (input.type === 'password') {
      input.type = 'text';
      eyeIcon.classList.remove('fa-eye-slash');
      eyeIcon.classList.add('fa-eye');
    } else {
      input.type = 'password';
      eyeIcon.classList.remove('fa-eye');
      eyeIcon.classList.add('fa-eye-slash');
    }
  }
}