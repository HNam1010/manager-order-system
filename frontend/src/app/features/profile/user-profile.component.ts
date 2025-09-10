import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MustMatch } from '../../core/helpers/must-match.validator';
import { StorageService } from '../../core/services/storage.service';
import { AuthService } from '../../service/auth.service';
import { UserInfo, UserProfileUpdateRequest } from '../../models/auth.model';
import { UserResponse } from '../../models/user.model';
import { ReactiveFormsModule } from '@angular/forms';
import { inject } from '@angular/core';
import { HeaderComponent } from '../../layout/header/header.component';
import { FooterComponent } from '../../layout/footer/footer.component';

@Component({
    selector: 'app-user-profile',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterModule,
        HeaderComponent,
        FooterComponent
    ],
    templateUrl: './user-profile.component.html',
    styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit, OnDestroy {

    currentUser: UserInfo | null = null; // Dữ liệu user
    profileForm: FormGroup;
    isLoading: boolean = false;
    errorMessage: string | null = null;
    successMessage: string | null = null;
    isSaving: boolean = false;

    private userSubscription: Subscription | null = null;
    private updateSubscription: Subscription | null = null;
    private authService: AuthService = inject(AuthService);
 
    constructor(
        private fb: FormBuilder,
    ) {
        this.profileForm = this.fb.group({
            username: [{ value: '', disabled: true }], // Không cho sửa
            email: ['', [Validators.required, Validators.email]],
            fullName: [''], 
            phone: [''],
            address: [''],
            birthDay: [''],
            newPassword: ['', Validators.minLength(6)],
            confirmPassword: ['']
        }, {
            validators: MustMatch('newPassword', 'confirmPassword')  // Custom validator
        });
    }

    ngOnInit(): void {
        this.loadUserProfile();
    }

    ngOnDestroy(): void {
        this.userSubscription?.unsubscribe();
        this.updateSubscription?.unsubscribe();
    }

    loadUserProfile(): void {
        this.isLoading = true;
        this.errorMessage = null;
        this.successMessage = null;

        this.authService.getCurrentUserProfile().subscribe({
            next: (user) => {
                this.currentUser = user;
                this.profileForm.patchValue({
                    username: user.username,
                    email: user.email,
                    fullName: user.fullName, // Patch các giá trị mới
                    phone: user.phone,
                    address: user.address,
                    birthDay: user.birthDay // YYYY-MM-DD
                });

                this.isLoading = false;
                console.log('User profile loaded:', this.currentUser);
            },
            error: (err) => {
                this.errorMessage = err.message || 'Không thể tải thông tin người dùng.';
                this.isLoading = false;
                console.error('Error loading user profile:', err);
            }
        });
    }

    onSubmit(): void {
        if (this.profileForm.invalid) {
            console.warn('Form is invalid. Not submitting.');
            return;
        }
        this.isSaving = true;
        this.errorMessage = null;
        this.successMessage = null;

        const updateData: UserProfileUpdateRequest = {
            email: this.profileForm.get('email')?.value,
            fullName: this.profileForm.get('fullName')?.value,
            phone: this.profileForm.get('phone')?.value,
            address: this.profileForm.get('address')?.value,
            birthDay: this.profileForm.get('birthDay')?.value,
        };

        if (this.profileForm.get('newPassword')?.value) { // Kiểm tra newPassword không rỗng
            if (this.profileForm.get('newPassword')?.value === this.profileForm.get('confirmPassword')?.value) {
                updateData.newPassword = this.profileForm.get('newPassword')?.value;
            } else {
                this.errorMessage = "Mật khẩu mới và mật khẩu xác nhận không khớp.";
                this.isSaving = false;
                return;
            }
        }

        console.log('Submitting updated profile data:', updateData); // Log dữ liệu gửi đi

        this.updateSubscription = this.authService.updateCurrentUserProfile(updateData).subscribe({
            next: (updatedUser) => {
                console.log('Profile updated successfully:', updatedUser);
                this.successMessage = 'Cập nhật thông tin thành công!';
                this.updateCurrentUserState(updatedUser); // Cập nhật state trong service và storage
                this.isSaving = false;
            },
            error: (err) => {
                console.error('Error updating profile:', err);
                this.errorMessage = err.message || 'Lỗi cập nhật thông tin cá nhân.';
                this.isSaving = false;
            }
        });
    }

    // Cập nhật thông tin user hiện tại trong service và storage 
    updateCurrentUserState(newUser: UserInfo): void {
        this.authService.updateCurrentUserState(newUser);
    }


    isFieldInvalid(fieldName: string): boolean {
        const control = this.profileForm.get(fieldName);
        return !!(control && control.invalid && (control.dirty || control.touched));
    }

 
}