import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
// THÊM: Router, Subscription
import { Router, RouterLink } from '@angular/router'; // Giữ RouterLink
import { Subscription } from 'rxjs';

// Import các service cần thiết
import { AuthService } from '../../service/auth.service'; 

// Import layout components
import { FooterComponent } from '../../layout/footer/footer.component';
import { HeaderComponent } from '../../layout/header/header.component';
import { RegisterRequest } from '../../models/auth.model'; 

//Hàm xác thực tùy chỉnh để khớp mật khẩu
export function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

     if (!password || !confirmPassword || password.pristine || confirmPassword.pristine) {
        return null;
    }
 
    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
}


@Component({
    selector: 'app-register',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterLink,
        HeaderComponent,
        FooterComponent
    ],
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss'] // Sửa thành styleUrls
})
 
export class RegisterComponent implements OnInit, OnDestroy {

    registerForm: FormGroup;
    isLoading = false;
    isSuccessful = false;
    errorMessage = '';
 
    private registerSubscription: Subscription | null = null;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.registerForm = this.fb.group({
            // THÊM FormControl cho username
            username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
            email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
            password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(120)]],
            confirmPassword: ['', [Validators.required]],
 
            agreeTerms: [false, [Validators.requiredTrue]] // Phải được check
        }, { validators: passwordMatchValidator }); // Áp dụng custom validator ở cấp FormGroup
    }

    ngOnInit(): void {}

    ngOnDestroy(): void {
        this.registerSubscription?.unsubscribe();
    }

    onSubmit(): void {
        this.errorMessage = '';
        this.isSuccessful = false;

        if (this.registerForm.invalid) {
            this.registerForm.markAllAsTouched();
            return;
        }

        this.isLoading = true;

        const { username, email, password } = this.registerForm.value;
        const registerPayload: RegisterRequest = { username, email, password };

        this.registerSubscription = this.authService.register(registerPayload).subscribe({
            next: (data) => {
                this.isLoading = false;
                this.isSuccessful = true;
                alert('Đăng ký thành công! Vui lòng đăng nhập.');
                this.router.navigate(['/login']); // Chuyển hướng đến trang đăng nhập
            },
            error: (err) => {
                this.isLoading = false;
                this.errorMessage = err.message || 'Đăng ký thất bại. Vui lòng thử lại.';
                this.isSuccessful = false;
                // alert(this.errorMessage);
            }
        });
    }

    // Copy hàm togglePasswordVisibility từ LoginComponent nếu muốn
    togglePasswordVisibility(passwordInput: HTMLInputElement, icon: HTMLElement): void {
        if (passwordInput.type === 'password') {
          passwordInput.type = 'text';
          icon.classList.remove('fa-eye-slash');
          icon.classList.add('fa-eye');
        } else {
          passwordInput.type = 'password';
          icon.classList.remove('fa-eye');
          icon.classList.add('fa-eye-slash');
        }
      }
}