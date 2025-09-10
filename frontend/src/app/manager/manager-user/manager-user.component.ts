import { Component, OnInit, OnDestroy, AfterViewInit, ElementRef, ViewChild, Inject, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule } from '@angular/forms';
import { Subscription, Observable, throwError } from 'rxjs';
import * as bootstrap from 'bootstrap';
import { catchError, finalize, switchMap, map } from 'rxjs/operators';
import { UserService } from '../../service/user.service';
import { AuthService } from '../../service/auth.service';

import { Page } from '../../models/page.model';
import { User, Role, UserCreateRequest, UserUpdateRequest } from '../../models/user.model';
import { ManagerSidebarComponent } from '../layout/manager-sidebar/manager-sidebar.component';
import { FooterComponent } from '../../layout/footer/footer.component';

@Component({
  selector: 'app-manager-user',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    FooterComponent,
    ManagerSidebarComponent
  ],
  templateUrl: './manager-user.component.html',
  styleUrls: ['./manager-user.component.scss']
})
export class ManagerUserComponent implements OnInit, OnDestroy, AfterViewInit {

  users: User[] = [];
  isLoading: boolean = false;
  errorMessage: string | null = null;
  private userSubscription: Subscription | null = null;
  private roleSubscription: Subscription | null = null;
  private modalSubscription: Subscription | null = null;


  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;

  userForm: FormGroup;
  isEditMode: boolean = false;
  editingUserId: number | null = null;
  isSaving: boolean = false;

  roles: Role[] = [];

  @ViewChild('userModal') userModalElementRef: ElementRef | undefined;
  private userModalInstance: bootstrap.Modal | null = null;
  private platformId = inject(PLATFORM_ID); // Inject PLATFORM_ID

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private fb: FormBuilder,
  ) {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      password: [''],
      role: [null, [Validators.required]],
      birthDay: [''],
      address: ['']
    });
  }

  ngOnInit(): void {
    this.loadUsers();
    this.loadRoles();
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId) && this.userModalElementRef) {
      this.userModalInstance = new bootstrap.Modal(this.userModalElementRef.nativeElement);
    }
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
    this.roleSubscription?.unsubscribe();
    this.modalSubscription?.unsubscribe();
    if (this.userModalInstance) {
      this.userModalInstance.dispose();
    }
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.userSubscription = this.userService.getUsers(this.currentPage, this.pageSize) //, this.selectedRoleId
      .subscribe({
        next: (pageData: Page<User>) => {
          this.users = pageData.content;
          // KIỂM TRA VÀ GÁN GIÁ TRỊ HỢP LỆ cho page
          this.totalElements = typeof pageData.totalElements === 'number' ? pageData.totalElements : 0;
          this.totalPages = typeof pageData.totalPages === 'number' ? pageData.totalPages : 0; // Gán 0 nếu không hợp lệ
          this.currentPage = typeof pageData.number === 'number' ? pageData.number : 0; // Gán 0 nếu không hợp lệ

          this.isLoading = false;
        },
        error: (err: any) => {
          this.errorMessage = err?.error?.message || err?.message || 'Không thể tải danh sách người dùng.';
          this.isLoading = false;
          this.users = [];
        }
      });
  }

  loadRoles(): void { // Giữ nguyên hàm này
    this.roleSubscription = this.userService.getRoles().subscribe({
      next: (rolesData: Role[]) => {
        this.roles = rolesData;
      },
      error: (err: any) => {
        console.error("Error loading roles:", err); // Log lỗi
        this.errorMessage = this.errorMessage || 'Lỗi tải danh sách vai trò.';
        this.roles = [];
      }
    });
  }


  addUser(): void {
    this.isEditMode = false;
    this.editingUserId = null;
    this.userForm.reset({ role: null });
    this.userForm.controls['password'].setValidators([Validators.required, Validators.minLength(6)]);
    this.userForm.controls['password'].updateValueAndValidity();
    this.userForm.controls['username'].enable();
    this.errorMessage = null;
    this.showModal();
    this.loadUsers();
  }

  editUser(user: User): void {
    this.isEditMode = true;
    this.editingUserId = user.id;
    this.userForm.reset();
    this.userForm.patchValue({
      username: user.username,
      email: user.email,
      role: user.roles && user.roles.length > 0 ? user.roles[0] : null,
      birthDay: user.birthDay,
      address: user.address
    });
    this.userForm.controls['password'].clearValidators();
    this.userForm.controls['password'].updateValueAndValidity();
    this.userForm.controls['username'].disable();
    this.errorMessage = null;
    this.showModal();
    this.loadUsers();
  }

  saveUser(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }
    this.isSaving = true;
    this.errorMessage = null;

    const formData = this.userForm.getRawValue();
    let apiCall: Observable<User | any>;

    if (this.isEditMode && this.editingUserId !== null) {
      const updateData: UserUpdateRequest = {
        email: formData.email,
        role: formData.role,
        birthDay: formData.birthDay,
        address: formData.address
      };
      apiCall = this.userService.updateUser(this.editingUserId, updateData);
    } else {
      const createData: UserCreateRequest = {
        username: formData.username,
        password: formData.password,
        email: formData.email,
        role: formData.role,
        birthDay: formData.birthDay,
        address: formData.address
      };
      apiCall = this.authService.register(createData);
    }

    this.modalSubscription = apiCall.pipe(
      finalize(() => this.isSaving = false)
    ).subscribe({
      next: () => {
        alert(this.isEditMode ? 'Cập nhật thành công!' : 'Thêm người dùng thành công!');
        this.hideModal();
        this.loadUsers();
      },
      error: (err: any) => {
        this.errorMessage = err.message || 'Lỗi lưu người dùng.';
      }
    });

    this.loadUsers();
  }

  deleteUser(userId: number): void {
    if (!confirm(`Bạn có chắc chắn muốn xóa người dùng có ID ${userId}?`)) return;
    this.isLoading = true;
    this.errorMessage = null;
    this.userService.deleteUser(userId).subscribe({
      next: (message) => {
        alert(message);
        this.loadUsers();
      },
      error: (err: any) => {
        this.errorMessage = err.message || 'Lỗi xóa người dùng.';
        this.isLoading = false;
        alert(this.errorMessage);
      },
      complete: () => {
        this.isLoading = false;
      }
    });
    this.loadUsers();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.loadUsers();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadUsers();
    }
  }

  private showModal(): void {
    if (isPlatformBrowser(this.platformId) && this.userModalInstance) {
      this.userModalInstance.show();
    }
  }

  private hideModal(): void {
    if (isPlatformBrowser(this.platformId) && this.userModalInstance) {
      this.userModalInstance.hide();
    }
  }

  closeModal(): void {
    this.hideModal();
  }
}