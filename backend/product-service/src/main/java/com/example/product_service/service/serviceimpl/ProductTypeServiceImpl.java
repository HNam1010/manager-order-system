package com.example.product_service.service.serviceimpl;


import com.example.product_service.dto.request.ProductTypeRequest;
import com.example.product_service.dto.response.ProductTypeResponse;
import com.example.product_service.entity.ProductType;
import com.example.product_service.repository.ProductTypeRepository;
import com.example.product_service.service.servicerepo.ProductTypeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductTypeServiceImpl implements ProductTypeService {
    private final ProductTypeRepository productTypeRepository;

    // Constructor duy nhất để Spring autowire
    public ProductTypeServiceImpl(ProductTypeRepository productTypeRepository) {
        this.productTypeRepository = productTypeRepository;
    }

    @Override
    public ProductTypeResponse createProductType(ProductTypeRequest request) {
        ProductType productType = new ProductType();
        productType.setName(request.getName());

        ProductType saved = productTypeRepository.save(productType);
        return mapToResponse(saved);
    }

    @Override
    public ProductTypeResponse getById(Integer id) {
        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy loại sản phẩm với ID: " + id));
        return mapToResponse(productType);
    }

    @Override
    public Page<ProductTypeResponse> getAll(java.awt.print.Pageable pageable) {
        return null;
    }

    @Override
    public Page<ProductTypeResponse> getAll(Pageable pageable) {
        return productTypeRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public ProductTypeResponse update(Integer id, ProductTypeRequest request) {
        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy loại sản phẩm với ID: " + id));

        productType.setName(request.getName());

        ProductType updated = productTypeRepository.save(productType);
        return mapToResponse(updated);
    }

    @Override
    public void delete(Integer id) {
        if (!productTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy loại sản phẩm với ID: " + id);
        }
        productTypeRepository.deleteById(id);
    }

    private ProductTypeResponse mapToResponse(ProductType productType) {
        ProductTypeResponse response = new ProductTypeResponse();
        response.setSerialId(productType.getSerialId());
        response.setName(productType.getName());
        return response;
    }
}
