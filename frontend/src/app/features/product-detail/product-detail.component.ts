// src/app/product-detail/product-detail.component.ts
import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID, HostListener, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common'; // Import Location
import { ActivatedRoute, Router, RouterModule, RouterLink } from '@angular/router'; // Import ActivatedRoute, Router, RouterModule
import { Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';


// Import Service và Models
import { ProductService } from '../../service/product.service';
import { Product } from '../../models/product.model';
import { CartService } from '../../service/cart.service';


// Import Header và Footer 
import { FooterComponent } from '../../layout/footer/footer.component';
import { HeaderComponent } from '../../layout/header/header.component';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    HeaderComponent,
    FooterComponent
  ],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit, OnDestroy {

  product: Product | null = null; // Sản phẩm chi tiết
  isLoading: boolean = false;
  errorMessage: string | null = null;
  private routeSubscription: Subscription | null = null;
  private productSubscription: Subscription | null = null;
  private addToCartSubscription: Subscription | null = null; // Thêm subscription cho addToCart

  //biến để sử dụng chức năng mua ngay
  private buyNowSubscription: Subscription | null = null;


  // --- Biến cho hiệu ứng Zoom ---
  showZoom: boolean = false; // Cờ để hiển thị/ẩn vùng zoom
  backgroundPosition: string = '0% 0%'; // Vị trí background cho ảnh zoom
  zoomPosition: { top: string; left: string } = { top: '0px', left: '0px' }; 



  // Base URL ảnh (giống HomeComponent)
  imageBaseUrl = 'http://localhost:8080/api/v1/products/images/';

  constructor(
    private route: ActivatedRoute, // Inject ActivatedRoute để lấy ID từ URL
    private productService: ProductService, // Inject ProductService
    private location: Location, // Inject Location để quay lại trang trước (tùy chọn)
    protected router: Router, // Inject Router để điều hướng nếu sản phẩm không tồn tại
    private cdr: ChangeDetectorRef,
    private cartService: CartService, // <-- Inject CartService
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    this.loadProductDetails();
  }

  ngOnDestroy(): void {
    this.routeSubscription?.unsubscribe();
    this.productSubscription?.unsubscribe();
    this.addToCartSubscription?.unsubscribe(); // Hủy cả subscription này

    //dùng để sử dụng nút mua ngay
    this.buyNowSubscription?.unsubscribe();
  }


  /** Xử lý Mua Ngay - MỚI */
  buyNow(product: Product): void {
    // 1. Kiểm tra sản phẩm hợp lệ và còn hàng
    if (!product || !product.serialId || product.quantity <= 0) {
      alert(product.quantity <= 0 ? `Sản phẩm "${product.name}" đã hết hàng.` : "Sản phẩm không hợp lệ.");
      return;
    }

    console.log(`Processing Buy Now for product: ID=${product.serialId}, Name=${product.name}`);
    const quantityToAdd = 1; // Mua ngay thường là mua 1 sản phẩm

    // Hủy subscription cũ (nếu có)
    this.buyNowSubscription?.unsubscribe();

    // 2. Gọi service để thêm vào giỏ hàng
    this.buyNowSubscription = this.cartService.addToCart(product.serialId, quantityToAdd).subscribe({
      next: (cartItem) => {
        console.log('Product added for Buy Now, navigating to cart:', cartItem);
        // 3. Sau khi thêm thành công, điều hướng đến trang giỏ hàng
        this.router.navigate(['/cart']);
      },
      error: (err) => {
        console.error('Error during Buy Now -> addToCart:', err);
        // Hiển thị lỗi, không điều hướng
        alert(`Lỗi khi chuẩn bị mua ngay: ${err.message}`);
      }
    });
  }


  loadProductDetails(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // Lấy ID từ tham số URL
    this.routeSubscription = this.route.paramMap.pipe(
      switchMap(params => {
        const idParam = params.get('id'); // Lấy giá trị của tham số ':id'
        console.log('ID Param from URL:', idParam);
        if (idParam) {
          const productId = +idParam; // Chuyển string ID thành number
          if (!isNaN(productId)) {
            console.log('Fetching product details for ID:', productId);
            // Gọi service để lấy chi tiết sản phẩm
            return this.productService.getProductById(productId);
          } else {
            console.error('Invalid product ID parameter:', idParam);
            this.handleNotFoundError('ID sản phẩm không hợp lệ.');
            return []; // Trả về Observable rỗng để dừng pipe
          }
        } else {
          console.error('Product ID parameter not found in URL.');
          this.handleNotFoundError('Không tìm thấy ID sản phẩm trong đường dẫn.');
          return []; // Trả về Observable rỗng
        }
      })).subscribe({
        next: (productData) => { this.product = productData; this.isLoading = false; },
        error: (err) => { /* ... xử lý lỗi ... */
          this.isLoading = false;
          if (err.status === 404 || err.message?.toLowerCase().includes('not found')) { this.handleNotFoundError(`Không tìm thấy sản phẩm.`); }
          else { this.errorMessage = err.message || 'Không thể tải chi tiết sản phẩm.'; }
        }
      });
  }

  // Hàm xử lý khi không tìm thấy sản phẩm
  handleNotFoundError(message: string): void {
    this.errorMessage = message;
  }

  /** Xây dựng URL đầy đủ cho ảnh (giống HomeComponent) */
  getProductImageUrl(imagePath: string | undefined | null): string {
    const placeholder = 'assets/images/placeholder.png'; // Đường dẫn ảnh mặc định

    const baseUrl = 'http://localhost:8080/api/v1/products/images'; // Base URL không có / cuối

    if (!imagePath || imagePath.trim() === '') {
      return placeholder;
    }

    // Xử lý nếu imagePath bắt đầu bằng /
    const cleanImagePath = imagePath.startsWith('/') ? imagePath.substring(1) : imagePath;

    // Nối chuỗi an toàn
    const finalUrl = `${baseUrl}/${cleanImagePath}`; // Luôn có 1 dấu /

    console.log(`Generating image URL: ${finalUrl}`);
    return finalUrl;
  }

  /** Quay lại trang trước (ví dụ) */
  goBack(): void {
    this.location.back();
  }

  /** Thêm vào giỏ hàng */
  addToCart(): void {
    // 1. Kiểm tra xem dữ liệu sản phẩm đã được tải chưa
    if (!this.product || !this.product.serialId) {
      console.error("addToCart called but product data is not available or invalid.");
      alert("Không thể thêm sản phẩm này vào giỏ hàng (dữ liệu chưa sẵn sàng).");
      return;
    }

    // 2. Kiểm tra xem còn hàng không (tùy chọn nhưng nên có)
    if (this.product.quantity <= 0) {
      alert("Sản phẩm này đã hết hàng.");
      return;
    }

    console.log(`Adding product to cart from detail page: ID=${this.product.serialId}, Name=${this.product.name}`);

    // 3. Lấy số lượng cần thêm (mặc định là 1, có thể lấy từ input nếu có)
    const quantityToAdd = 1; 

    // Hủy subscription cũ nếu có để tránh gọi nhiều lần nếu user click nhanh
    this.addToCartSubscription?.unsubscribe();

    // 4. Gọi CartService
    this.addToCartSubscription = this.cartService.addToCart(this.product.serialId, quantityToAdd).subscribe({
      next: (cartItem) => {
        console.log('Product added to cart successfully from detail page:', cartItem);
        // Hiển thị thông báo thành công
        alert(`Đã thêm ${quantityToAdd} "${this.product?.name}" vào giỏ hàng!`);
        // Service sẽ tự cập nhật số lượng trên header
      },
      error: (err) => {
        console.error('Error adding product to cart from detail page:', err);
        // Hiển thị thông báo lỗi
        alert(`Lỗi thêm vào giỏ hàng: ${err.message}`);
      }
    });
  }


  // --- HÀM XỬ LÝ SỰ KIỆN CHUỘT CHO ZOOM ---
  // Hàm này được gọi khi chuột di chuyển trên ảnh chính
  onMouseMove(event: MouseEvent): void {
    console.log('Mouse move event triggered', event);

    this.showZoom = true;

    const imgElement = event.target as HTMLImageElement;
    const rect = imgElement.getBoundingClientRect();

    let x = (event.clientX - rect.left) / rect.width;
    let y = (event.clientY - rect.top) / rect.height;

    x = Math.max(0, Math.min(1, x));
    y = Math.max(0, Math.min(1, y));

    this.backgroundPosition = `${x * 100}% ${y * 100}%`;
    console.log('Background Position:', this.backgroundPosition);

    const zoomSize = 100;
    const zoomX = event.clientX - zoomSize / 2;
    const zoomY = event.clientY - zoomSize / 2;
    this.zoomPosition = {
      left: `${zoomX}px`,
      top: `${zoomY}px`
    };
    console.log('Zoom Position:', this.zoomPosition);

    this.cdr.detectChanges();
  }

  onMouseLeave(): void {
    console.log('Mouse leave event triggered');
    this.showZoom = false;
    this.cdr.detectChanges();
  }

  onImageError(event: Event): void {
    console.error('Failed to load image:', event);
  }

}