package com.example.product_service.service.serviceimpl;

import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.UpdateStockRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductType;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.repository.ProductTypeRepository;
import com.example.product_service.service.servicerepo.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.be.commons.handler.exception.BadRequestException;
import com.example.be.commons.handler.exception.FileStorageException;
import com.example.be.commons.handler.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {


    // Các trường final được inject qua constructor bên dưới
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ProductMapper productMapper;
    private final Path rootLocation;

    // Khai báo Logger thủ công
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);


    // Constructor duy nhất để Spring sử dụng cho injection
    public ProductServiceImpl(ProductRepository productRepository,
                              ProductTypeRepository productTypeRepository,
                              // UserRepository userRepository, // Bỏ đi
                              ProductMapper productMapper,
                              @Value("${file.upload-dir}") String uploadDir) {
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
        this.productMapper = productMapper;
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
            log.info("Created product image upload directory: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            log.error("Could not initialize storage location: {}", uploadDir, e);
            throw new FileStorageException("Could not initialize storage", e);
        }
    }


    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, MultipartFile imageFile) {
        log.info("Attempting to create new product...");

        // 1. Lấy ProductType từ ID
        ProductType productType = productTypeRepository.findById(request.getProductTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductType not found with ID: " + request.getProductTypeId()));

        // 2. Xử lý lưu ảnh (nếu có)
        String imagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imagePath = storeFile(imageFile); // Gọi hàm lưu file
            log.info("Stored image file successfully. Path: {}", imagePath);
        }

        // 3. Tạo đối tượng Product mới
        Product product = new Product();
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setProductType(productType);
        product.setUserId(request.getUserId());
        product.setSize(request.getSize());

        if (imagePath != null) {
            product.setImagePath(imagePath);
        }

        // 4. createdAt và updatedAt sẽ được tự động set trong entity
        log.info("Product before save: Name={}, Brand={}, Price={}, Quantity={}, ProductTypeId={}, UserId={}, ImagePath={}",
                product.getName(), product.getBrand(), product.getPrice(), product.getQuantity(),
                product.getProductType().getSerialId(), product.getUserId(), product.getImagePath());
        // 5. Lưu vào DB (serialId sẽ tự sinh, KHÔNG set thủ công)
        Product savedProduct = productRepository.save(product); // Dòng 153
        log.info("Product created successfully with ID: {}", savedProduct.getSerialId());

        return productMapper.toResponse(savedProduct);
    }


    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với ID: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable, String search, Long typeId) { // <-- THÊM search và typeId
        log.info("Fetching products: Page={}, Size={}, Sort={}, Search='{}', TypeId={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), search, typeId);

        Page<Product> productPage;
        //SỬ DỤNG search và typeId ĐỂ GỌI REPOSITORY
        // Kiểm tra xem có cần tìm kiếm/lọc hay không
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasFilter = typeId != null;

        if (hasSearch || hasFilter) {
            // Gọi phương thức repository có khả năng tìm kiếm và lọc Đảm bảo tên phương thức này tồn tại trong ProductRepository
            productPage = productRepository.searchAndFilterProducts(search, typeId, pageable);
        } else {
            // Nếu không tìm kiếm/lọc, gọi phương thức findAll có fetch
            productPage = productRepository.findAllWithDetails(pageable); // query fetch
            // productPage = productRepository.findAll(pageable); // Hoặc findAll cơ bản
        }

        log.info("Found {} products matching criteria.", productPage.getTotalElements());
        // Map kết quả sang DTO
        return productPage.map(productMapper::toResponse);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest, MultipartFile imageFile) {
        // Tìm sản phẩm hiện có
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với ID: " + id));

        // Kiểm tra tên mới có trùng với sản phẩm khác không
        productRepository.findByName(productRequest.getName())
                .filter(p -> !Objects.equals(p.getSerialId(), id)) // Chỉ kiểm tra với các sản phẩm khác
                .ifPresent(p -> {
                    throw new BadRequestException("Tên sản phẩm '" + productRequest.getName() + "' đã tồn tại.");
                });

        // Cập nhật ProductType nếu ID thay đổi
        if (!Objects.equals(existingProduct.getProductType().getSerialId(), productRequest.getProductTypeId())) {
            ProductType newProductType = productTypeRepository.findById(productRequest.getProductTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sản phẩm với ID: " + productRequest.getProductTypeId()));
            existingProduct.setProductType(newProductType);
        }


        // Xử lý cập nhật ảnh
        String oldImagePath = existingProduct.getImagePath();
        String newImagePath = oldImagePath; // Giữ ảnh cũ nếu không có ảnh mới

        if (imageFile != null && !imageFile.isEmpty()) {
            // Lưu ảnh mới
            newImagePath = storeFile(imageFile);
            existingProduct.setImagePath(newImagePath); // Cập nhật đường dẫn ảnh mới
            log.info("Updating image for Product ID: {}. New image path: {}", id, newImagePath);

            // Xóa ảnh cũ chỉ khi ảnh mới đã được lưu thành công
            if (!newImagePath.equals(oldImagePath) && oldImagePath != null) {
                deleteStoredFile(oldImagePath); // Chuyển vào đây để chắc chắn ảnh mới OK
            }
        }

        //Cập nhật các trường khác
        existingProduct.setName(productRequest.getName());
        existingProduct.setBrand(productRequest.getBrand());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setQuantity(productRequest.getQuantity());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setSize(productRequest.getSize());
        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Updated Product with ID: {}", id);

        // Trả về response
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        // Tìm sản phẩm
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với ID: " + id));

        // Lấy đường dẫn ảnh
        String imagePath = product.getImagePath();

        //  Xóa sản phẩm khỏi DB
        productRepository.delete(product);
        log.info("Đã xóa sản phẩm với ID: {}", id);

        // Xóa file ảnh liên quan (updat sau để thêm 1 sản phẩm được nhiều ảnh)
        if (imagePath != null) {
            deleteStoredFile(imagePath);
        }
    }

    // --- Help methods cho việc lưu và xóa file ---
    private String storeFile(MultipartFile file) {
        // Validate file cơ bản check
        if (file.isEmpty()) {
            throw new FileStorageException("Không thể lưu file rỗng.");
        }

        // Tạo tên file duy nhất để tránh trùng lặp dùng uuid để gắn thêm phía trước
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        try {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // Kiểm tra extension hợp lệ (ví dụ: .png, .jpg, .jpeg)
            if (!isValidImageExtension(fileExtension)) {
                throw new FileStorageException("Phần mở rộng file không hợp lệ: " + fileExtension);
            }
        } catch (Exception e) {
            throw new FileStorageException("Không thể lấy phần mở rộng file từ: " + originalFilename, e);
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        // Lưu file
        try {
            Path destinationFile = this.rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();

            // Kiểm tra bảo mật: Đảm bảo file được lưu trong thư mục gốc đã định nghĩa
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new FileStorageException("Không thể lưu file ngoài thư mục hiện tại.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                log.info("Stored file: {}", newFilename);
                return newFilename; // Trả về tên file đã lưu
            }
        } catch (IOException e) {
            log.error("Lỗi khi lưu file: {}", newFilename, e);
            throw new FileStorageException("Lỗi khi lưu file " + newFilename, e);
        }
    }

    private void deleteStoredFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            boolean deleted = Files.deleteIfExists(file);
            if(deleted) {
                log.info("Deleted stored file: {}", filename);
            } else {
                log.warn("Could not delete stored file (may not exist): {}", filename);
            }
        } catch (IOException e) {
            log.error("Lỗi khi xóa file: {}", filename, e);

        }
    }

    private boolean isValidImageExtension(String extension) {
        String lowerCaseExtension = extension.toLowerCase();
        return lowerCaseExtension.equals(".png") ||
                lowerCaseExtension.equals(".jpg") ||
                lowerCaseExtension.equals(".jpeg") ||
                lowerCaseExtension.equals(".gif"); // Thêm các định dạng khác nếu cần
    }


    @Override
    @Transactional // QUAN TRỌNG: Đảm bảo cập nhật kho là transaction
    public void decreaseStock(List<UpdateStockRequest> updateRequests) {
        if (updateRequests == null || updateRequests.isEmpty()) {
            log.warn("Received empty stock decrease request list.");
            return; // Không có gì để làm
        }
        log.info("Processing stock decrease for {} requests.", updateRequests.size());

        // Lấy danh sách các ID sản phẩm cần cập nhật
        List<Long> productIds = updateRequests.stream()
                .map(UpdateStockRequest::getProductId)
                .distinct()
                .collect(Collectors.toList());

        // Tải tất cả sản phẩm liên quan chỉ bằng một query để tối ưu
        Map<Long, Product> productsMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getSerialId, product -> product));

        List<Product> productsToUpdate = new ArrayList<>();

        for (UpdateStockRequest request : updateRequests) {
            Long productId = request.getProductId();
            Integer quantityToDecrease = request.getQuantityToDecrease();

            if (quantityToDecrease == null || quantityToDecrease <= 0) {
                log.warn("Skipping invalid stock decrease quantity for productId {}: {}", productId, quantityToDecrease);
                continue; // Bỏ qua nếu số lượng giảm không hợp lệ
            }

            Product product = productsMap.get(productId);

            if (product == null) {
                // Sản phẩm không tồn tại -> Lỗi nghiêm trọng
                log.error("Product with ID {} not found during stock decrease.", productId);
                throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId + " để cập nhật tồn kho.");
            }

            long currentStock = product.getQuantity();
            if (currentStock < quantityToDecrease) {
                // Số lượng tồn không đủ -> Lỗi nghiệp vụ
                log.error("Insufficient stock for product ID {}. Required decrease: {}, Available: {}", productId, quantityToDecrease, currentStock);
                throw new IllegalStateException("Không đủ tồn kho cho sản phẩm '" + product.getName() + "' (ID: " + productId + "). Yêu cầu giảm: " + quantityToDecrease + ", Hiện có: " + currentStock);
            }

            // Tính toán và cập nhật số lượng mới
            long newStock = currentStock - quantityToDecrease;
            product.setQuantity(newStock);
            productsToUpdate.add(product); // Thêm vào danh sách cần lưu
            log.info("Prepared stock update for Product ID: {}. New Quantity: {}", productId, newStock);
        }

        // Lưu tất cả sản phẩm đã thay đổi chỉ bằng một lệnh saveAll
        if (!productsToUpdate.isEmpty()) {
            productRepository.saveAll(productsToUpdate);
            log.info("Successfully updated stock for {} products.", productsToUpdate.size());
        } else {
            log.info("No valid stock updates to process.");
        }
    }
}
