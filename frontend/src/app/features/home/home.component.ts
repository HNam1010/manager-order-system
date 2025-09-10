import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { FooterComponent } from '../../layout/footer/footer.component';
import { HeaderComponent } from '../../layout/header/header.component';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterModule, Router } from '@angular/router';

import { Product, ProductType } from '../../models/product.model';
import { ProductService } from '../../service/product.service'; // Product Service
import { CartService } from '../../service/cart.service'; // Cart Service
import { CartItem } from '../../models/cart.model'; // CartItem model (cho response)
import { Page } from '../../models/page.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    FooterComponent,
    HeaderComponent,
    RouterLink,
    RouterModule,
    CommonModule,
    FormsModule,
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit, OnDestroy {
  products: Product[] = []; // Mảng sản phẩm hiển thị
  productTypes: ProductType[] = [];
  isLoading: boolean = false;
  errorMessage: string | null = null;
  private productsSubscription: Subscription | null = null;
  private typesSubscription: Subscription | null = null;
  private addToCartSubscription: Subscription | null = null;

  // --- THAM SỐ HIỂN THỊ ---
  currentPage: number = 0;
  pageSize: number = 9;
  totalPages: number = 0;
  totalElements: number = 0;

  // --- THAM SỐ TÌM KIẾM, LỌC, SẮP XẾP ---
  searchTerm: string = '';
  selectedProductTypeId: number | null = null;
  sortField: string = 'name'; // Mặc định sắp xếp theo tên
  sortDirection: 'asc' | 'desc' = 'asc'; // Mặc định tăng dần

  //biến để sử dụng chức năng mua ngay
  private buyNowSubscription: Subscription | null = null;

  imageBaseUrl = 'http://localhost:8080/api/v1/products/images/'; // Đã sửa theo FileController

  constructor(
    private productService: ProductService,
    private cartService: CartService, // Inject CartService
    private router: Router // Router ở đây để sử dụng hàm mua ngay
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    this.loadProductTypes();
  }

  ngOnDestroy(): void {
    this.productsSubscription?.unsubscribe();
    this.typesSubscription?.unsubscribe();
    this.addToCartSubscription?.unsubscribe();

    this.buyNowSubscription?.unsubscribe();
  }

  /*Xử lý Mua Ngay - MỚI */
  buyNow(product: Product): void {
    // 1. Kiểm tra sản phẩm hợp lệ và còn hàng
    if (!product || !product.serialId || product.quantity <= 0) {
      alert(
        product.quantity <= 0
          ? `Sản phẩm "${product.name}" đã hết hàng.`
          : 'Sản phẩm không hợp lệ.'
      );
      return;
    }

    console.log(
      `Processing Buy Now for product: ID=${product.serialId}, Name=${product.name}`
    );
    const quantityToAdd = 1; // Mua ngay thường là mua 1 sản phẩm

    // Hủy subscription cũ (nếu có)
    this.buyNowSubscription?.unsubscribe();

    // 2. Gọi service để thêm vào giỏ hàng
    this.buyNowSubscription = this.cartService
      .addToCart(product.serialId, quantityToAdd)
      .subscribe({
        next: (cartItem) => {
          console.log(
            'Product added for Buy Now, navigating to cart:',
            cartItem
          );
          // 3. Sau khi thêm thành công, điều hướng đến trang giỏ hàng
          this.router.navigate(['/cart']);
        },
        error: (err) => {
          console.error('Error during Buy Now -> addToCart:', err);
          // Hiển thị lỗi, không điều hướng
          alert(`Lỗi khi chuẩn bị mua ngay: ${err.message}`);
        },
      });
  }

  /** Tải danh sách sản phẩm từ backend, có hỗ trợ tìm kiếm và lọc */
  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = null;
    console.log(
      `>>> Loading products - Page: ${this.currentPage}, Size: ${this.pageSize}, Search: '${this.searchTerm}', TypeID: ${this.selectedProductTypeId}, Sort: ${this.sortField},${this.sortDirection}`
    );

    this.productsSubscription?.unsubscribe();
    this.productsSubscription = this.productService
      .getProducts(
        this.currentPage,
        this.pageSize,
        this.sortField, // <-- Truyền trường sắp xếp
        this.sortDirection, // <-- Truyền hướng sắp xếp
        this.searchTerm,
        this.selectedProductTypeId
      )
      .subscribe({
        next: (pageData) => {
          // Cập nhật dữ liệu và thông tin phân trang
          this.products = pageData.content;
          this.totalPages = pageData.totalPages;
          this.totalElements = pageData.totalElements;
          // Quan trọng: Cập nhật lại currentPage từ response phòng trường hợp yêu cầu trang không hợp lệ
          this.currentPage = pageData.number;
          this.isLoading = false;
          console.log(
            `>>> Products loaded: ${this.products.length} items. Total elements: ${this.totalElements}. Current page: ${this.currentPage}`
          );
        },
        error: (err) => {
          // Sử dụng message từ Error object ném ra bởi service
          this.errorMessage =
            err instanceof Error
              ? err.message
              : 'Không thể tải danh sách sản phẩm.';
          this.isLoading = false;
          console.error('>>> Error loading products:', this.errorMessage, err);
          // Reset dữ liệu khi có lỗi
          this.products = [];
          this.totalPages = 0;
          this.totalElements = 0;
          this.currentPage = 0;
        },
      });
  }

  loadProductTypes(): void {
    // Hủy sub cũ nếu có
    this.typesSubscription?.unsubscribe();
    this.typesSubscription = this.productService.getProductTypes().subscribe({
      next: (types) => {
        this.productTypes = types; // Gán dữ liệu lấy được
        console.log('Product Types for filter loaded:', this.productTypes);
      },
      error: (err) => {
        console.error('Error loading product types for filter:', err);

        this.errorMessage =
          'Không thể tải danh mục sản phẩm. Vui lòng thử lại sau.';
        this.productTypes = []; // Đảm bảo mảng rỗng khi có lỗi
      },
    });
  }

  /** Xây dựng URL đầy đủ cho ảnh */
  getProductImageUrl(imagePath: string | undefined | null): string {
    const baseUrl = 'http://localhost:8080/api/v1/products/images';
    const placeholder = 'assets/images/placeholder.png';

    const url = imagePath?.trim()
      ? `${baseUrl}/${imagePath.replace(/^\/+/, '')}`  // xoá mọi dấu /^\/+/ đầu tiên 
      : placeholder;

    console.log(`Generating image URL: ${url}`);
    return url;
  }

  // --- CÁC HÀM XỬ LÝ SỰ KIỆN MỚI ---
  /** Xử lý khi người dùng thay đổi tìm kiếm (có thể thêm debounce) */
  onSearchChange(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    this.searchTerm = inputElement.value;
    this.currentPage = 0; // Reset về trang đầu
    this.loadProducts(); // Tải lại với từ khóa mới
    console.log('Search term changed:', this.searchTerm);
  }

  /** Xử lý khi người dùng thay đổi bộ lọc loại sản phẩm */
  onCategoryChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const typeIdString = selectElement.value;

    let parsedId: number | null = null;
    // Chỉ parse khi typeIdString có giá trị và không phải là "null" (chuỗi)
    if (typeIdString && typeIdString !== 'null') {
      const num = parseInt(typeIdString, 10);
      if (!isNaN(num)) {
        // Kiểm tra xem parse có thành công không
        parsedId = num;
      } else {
        console.error(
          `Invalid non-numeric value received for typeId: ${typeIdString}`
        );
      }
    }
    this.selectedProductTypeId = parsedId;

    this.currentPage = 0;
    this.loadProducts(); // loadProducts sẽ gửi selectedProductTypeId (number hoặc null)
    console.log('Selected Category ID changed:', this.selectedProductTypeId);
  }

  /** Xử lý khi người dùng thay đổi cách sắp xếp */
  onSortChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const selectedValue = selectElement.value; // Ví dụ: "name,asc", "price,desc"

    if (selectedValue) {
      const parts = selectedValue.split(','); // Tách trường và hướng
      if (parts.length === 2) {
        this.sortField = parts[0];
        this.sortDirection = parts[1] as 'asc' | 'desc'; // Ép kiểu về 'asc' hoặc 'desc'
        this.currentPage = 0; // Reset về trang đầu
        this.loadProducts(); // Tải lại với sắp xếp mới
        console.log(
          `Sort changed: Field=${this.sortField}, Direction=${this.sortDirection}`
        );
      }
    }
  }

  // các hàm giỏ hàng
  /** Thêm vào giỏ hàng */
  addToCart(product: Product): void {
    if (!product || !product.serialId) {
      console.error('Invalid product data passed to addToCart:', product);
      alert('Không thể thêm sản phẩm không hợp lệ vào giỏ hàng.');
      return;
    }

    console.log('Adding product to cart:', product.serialId, product.name);

    if (product.quantity <= 0) {
      alert(`Sản phẩm "${product.name}" đã hết hàng.`);
      return;
    }
    const quantityToAdd = 1;

    this.addToCartSubscription?.unsubscribe();

    this.addToCartSubscription = this.cartService
      .addToCart(product.serialId, quantityToAdd)
      .subscribe({
        next: (cartItem) => {
          console.log(
            'Sản phẩm đã thêm vào giỏ hàng thành công từ trang chủ:',
            cartItem
          );
          alert(`Đã thêm ${quantityToAdd} "${product.name}" vào giỏ hàng!`);
        },
        error: (err) => {
          console.error(
            'Error Đã thêm sản phẩm từ trang chủ và giỏ hàng:',
            err
          );
          alert(`Lỗi thêm vào giỏ hàng: ${err.message}`);
        },
      });
  }

  // --- Các hàm phân trang (Giống component quản lý) ---
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
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
