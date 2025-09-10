// src/app/manager/manager-product.component.ts
import { Component, OnInit, OnDestroy, TemplateRef, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms'; // Đảm bảo import đúng
import { Subscription, Observable, throwError, of } from 'rxjs';
import { catchError, finalize, switchMap, map } from 'rxjs/operators';
import { AuthService } from '../../service/auth.service';


// Import NgbModal nếu dùng Ng-Bootstrap
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

// Import Service và Interfaces
import { ProductService } from '../../service/product.service'; // <-- Kiểm tra đường dẫn
import { Product, ProductType, ProductCreateRequest, ProductUpdateRequest } from '../../models/product.model'; // <-- Kiểm tra đường dẫn
import { Page } from '../../models/page.model';
//import { AuthService } from '../../service/auth.service'; // <-- THÊM: Import AuthService

// Import các component con (NÊN đặt ở layout cha)
import { ManagerSidebarComponent } from '../layout/manager-sidebar/manager-sidebar.component';
import { FooterComponent } from '../../layout/footer/footer.component';

@Component({
  selector: 'app-manager-product',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FooterComponent,
    ManagerSidebarComponent
  ],
  templateUrl: './manager-product.component.html',
  styleUrls: ['./manager-product.component.scss']
})
export class ManagerProductComponent implements OnInit, OnDestroy {

  //danh sách sản phẩm
  products: Product[] = [];

  // Danh sách Loại sản phẩm
  productTypes: ProductType[] = [];
  isLoading: boolean = false;
  errorMessage: string | null = null;
  private productSubscription: Subscription | null = null;
  private typeSubscription: Subscription | null = null;
  private formSubscription: Subscription | null = null;
  private modalSubscription: Subscription | null = null;

  // Phân trang
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;


  // Form Thêm/Sửa
  productForm: FormGroup; // Bỏ dấu !
  isEditMode: boolean = false;
  editingProductId: number | null = null;
  isSaving: boolean = false;



  // Ảnh
  selectedFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = null;
  currentImagePath: string | null = null;

  // NgbModal Ref
  private currentModalRef: NgbModalRef | null = null;

  constructor(
    private productService: ProductService,
    private fb: FormBuilder,
    private modalService: NgbModal, // Inject NgbModal
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object // Giữ lại nếu cần isPlatformBrowser cho việc khác
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required]],
      brand: [''],
      description: [''],
      price: [null, [Validators.required, Validators.min(0)]],
      quantity: [null, [Validators.required, Validators.min(0)]],
      productTypeId: [null, [Validators.required]], 
      size: ['', Validators.maxLength(50)]
 
    });
  }

  ngOnInit(): void {
    console.log('>>> ngOnInit: Start');
    this.loadProducts();
    this.loadProductTypes();
    console.log('>>> ngOnInit: End');
  }

  ngOnDestroy(): void {
    this.productSubscription?.unsubscribe();
    this.typeSubscription?.unsubscribe();
    this.formSubscription?.unsubscribe();
    this.modalSubscription?.unsubscribe();
    this.currentModalRef?.close(); // Đóng modal Ngb nếu còn mở
  }

  /** Tải danh sách sản phẩm */
  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = null;
    console.log(`>>> loadProducts: Fetching page ${this.currentPage}, size ${this.pageSize}`);
    this.productSubscription?.unsubscribe();
    this.productSubscription = this.productService.getProducts(this.currentPage, this.pageSize)
      .subscribe({
        next: (pageData: Page<Product>) => {
          this.products = pageData.content;
          this.totalElements = pageData.totalElements;
          this.totalPages = pageData.totalPages;
          console.log(`>>> loadProducts: Loaded ${this.products.length} products.`);
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('>>> loadProducts: Error:', err);
          this.errorMessage = 'Không thể tải danh sách sản phẩm.';
          this.isLoading = false;
          this.products = [];
          this.totalElements = 0;
          this.totalPages = 0;
        }
      });
  }

  /** Tải danh sách loại sản phẩm */
  loadProductTypes(): void {
    console.log('>>> loadProductTypes: Loading Product Types...');
    this.typeSubscription?.unsubscribe();
    this.typeSubscription = this.productService.getProductTypes().subscribe({
      next: (types) => {
        this.productTypes = types;
        console.log('Product Types loaded:', this.productTypes);
      },
      error: (err: any) => {
        console.error('Error loading product types:', err);
        alert(`Lỗi: Không thể tải danh sách loại sản phẩm.\nChi tiết: ${err.message}`);
        this.productTypes = [];
      }
    });
  }


  /** Xử lý chọn file */
  onFileSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList && fileList.length > 0) {
      this.selectedFile = fileList[0];
      const reader = new FileReader();
      reader.onload = e => this.imagePreview = reader.result;
      reader.readAsDataURL(this.selectedFile);
      element.value = '';
    } else {
      this.selectedFile = null;
      this.imagePreview = this.currentImagePath;
    }
  }

  /** Mở modal thêm */
  addProduct(content: TemplateRef<any>): void {
    this.isEditMode = false;
    this.editingProductId = null;
    this.productForm.reset(); // Reset sạch
    this.selectedFile = null;
    this.imagePreview = null;
    this.currentImagePath = null;
    this.errorMessage = null;
    this.openModal(content);
  }

  /** Mở modal sửa */
  editProduct(content: TemplateRef<any>, product: Product): void {
    this.isEditMode = true;
    this.editingProductId = product.serialId;
    this.selectedFile = null;
    this.currentImagePath = product.imagePath || null;
    this.imagePreview = this.currentImagePath;
    this.errorMessage = null;

    this.productForm.reset();
    this.productForm.patchValue({
      name: product.name,
      brand: product.brand,
      description: product.description,
      price: product.price,
      quantity: product.quantity,
      productTypeId: product.productTypeId,
      size: product.size 
    });
    this.openModal(content);
  }

  /** Đóng modal hiện tại */
  closeModal(): void {
    this.currentModalRef?.dismiss('Cancel click');
  }

  /** Hàm chung để mở modal NgbModal */
  openModal(content: TemplateRef<any>): void {
    this.currentModalRef = this.modalService.open(content, {
      ariaLabelledBy: 'productModalLabel',
      size: 'lg',
      centered: true,
      backdrop: 'static',
      keyboard: false
    });
    this.currentModalRef.result.then(
      (result) => { console.log(`Modal closed: ${result}`); this.resetModalState(); },
      (reason) => { console.log(`Modal dismissed: ${reason}`); this.resetModalState(); }
    );
  }

  /** Reset trạng thái khi modal đóng */
  resetModalState(): void {
    this.isEditMode = false;
    this.editingProductId = null;
    this.selectedFile = null;
    this.imagePreview = null;
    this.currentImagePath = null;
    this.errorMessage = null;
    this.isSaving = false;
    this.currentModalRef = null;
  }

  /** Lưu sản phẩm */
  saveProduct(): void {
    // 1. Validate form
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      this.errorMessage = "Vui lòng điền đầy đủ thông tin bắt buộc.";
      return;
    }

    // 2. Set trạng thái đang lưu, xóa lỗi cũ
    this.isSaving = true;
    this.errorMessage = null;

    // 3. Lấy UserID và chuẩn bị dữ liệu
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser || currentUser.id === undefined || currentUser.id === null) {
      this.errorMessage = "Lỗi: Không thể xác định người dùng Admin.";
      this.isSaving = false;
      alert(this.errorMessage);
      return;
    }
    const currentUserId = currentUser.id;
    console.log(`Admin User ID: ${currentUserId}`);

    //  ĐỊNH NGHĨA formValues  
    const formValues = this.productForm.value;
 


    // 4. Xác định API call
    let apiCall: Observable<Product>;

    if (this.isEditMode && this.editingProductId !== null) {
      //  Chế độ SỬA 
      const updateData: ProductUpdateRequest = {
        name: formValues.name, // Sử dụng formValues
        brand: formValues.brand || undefined,
        description: formValues.description,
        price: Number(formValues.price),
        quantity: Number(formValues.quantity),
        productTypeId: Number(formValues.productTypeId),
        size: formValues.size || undefined
       };
      console.log('>>> Updating Product ID:', this.editingProductId);
      console.log('>>> Update Data (before send):', JSON.stringify(updateData, null, 2));
      console.log('>>> Selected File (for update):', this.selectedFile);
      apiCall = this.productService.updateProduct(this.editingProductId, updateData, this.selectedFile);

    } else {
      //  Chế độ THÊM 
      const createData: ProductCreateRequest = {
        name: formValues.name, // Sử dụng formValues
        brand: formValues.brand || undefined,
        description: formValues.description,
        price: Number(formValues.price),
        quantity: Number(formValues.quantity),
        productTypeId: Number(formValues.productTypeId),
        size: formValues.size || undefined, // Lấy size từ form
        userId: currentUserId, // userId là bắt buộc khi tạo
      };
      console.log('>>> Creating New Product');
      console.log('>>> Final createData JSON string (before send):', JSON.stringify(createData, null, 2));
      console.log('>>> Selected File (for create):', this.selectedFile);
      apiCall = this.productService.createProduct(createData, this.selectedFile);
    }

    // 5. Thực hiện gọi API
    this.modalSubscription?.unsubscribe();
    this.modalSubscription = apiCall.pipe(
      finalize(() => { // finalize đã được import đúng
        console.log("Save/Update operation finished.");
        this.isSaving = false;
      })
    ).subscribe({
      next: (savedProduct: Product) => {
        console.log('Product saved/updated successfully:', savedProduct);
        alert(this.isEditMode ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!');
        this.closeModal(); // Đóng modal
        this.loadProducts(); // Tải lại danh sách
      },
      error: (err: any) => {
        this.errorMessage = err instanceof Error ? err.message : 'Lỗi không xác định khi lưu sản phẩm.';
        console.error('Error saving product:', this.errorMessage, err);
        alert(`Lỗi lưu sản phẩm: ${this.errorMessage}`);
      }
    });
  }


  //xóa sản phẩm
  deleteProduct(productId: number): void {
    if (!confirm(`Xóa sản phẩm ID ${productId}? Hành động này không thể hoàn tác!`)) { // Thêm cảnh báo rõ ràng hơn
      return;
    }
    // Không cần set isLoading ở đây nếu không có phản hồi trực quan ngay lập tức
    this.errorMessage = null; // Xóa lỗi cũ

    this.productService.deleteProduct(productId).subscribe({
      next: () => { // <-- Không còn tham số 'message' ở đây
        console.log(`Product ID ${productId} deleted successfully via service.`);
        alert(`Đã xóa thành công sản phẩm ID ${productId}`); // Hiển thị thông báo thành công
 
        this.loadProducts();
 
      },
      error: (err: any) => {
        this.errorMessage = err instanceof Error ? err.message : 'Lỗi không xác định khi xóa sản phẩm.'; // Lấy message từ Error object
        console.error('Error deleting product:', this.errorMessage, err);
        alert(`Lỗi xóa sản phẩm: ${this.errorMessage}`);
 
      }
    });
  }

  // --- Các hàm phân trang ---
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) { // Thêm check !== currentPage
      this.currentPage = page;
      this.loadProducts();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadProducts();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadProducts();
    }
  }

} 