package com.example.product_service.mapper;

import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductType;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-09T16:01:04+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.43.0.v20250819-1513, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse productResponse = new ProductResponse();

        productResponse.setSerialId( product.getSerialId() );
        productResponse.setName( product.getName() );
        productResponse.setBrand( product.getBrand() );
        productResponse.setImagePath( product.getImagePath() );
        productResponse.setPrice( product.getPrice() );
        productResponse.setUpdateDate( product.getUpdatedAt() );
        productResponse.setQuantity( product.getQuantity() );
        productResponse.setDescription( product.getDescription() );
        productResponse.setSize( product.getSize() );
        productResponse.setProductTypeId( productProductTypeSerialId( product ) );
        productResponse.setProductTypeName( productProductTypeName( product ) );
        productResponse.setUserId( product.getUserId() );
        productResponse.setCreatedDate( product.getCreatedAt() );

        return productResponse;
    }

    @Override
    public List<ProductResponse> toResponseList(List<Product> products) {
        if ( products == null ) {
            return null;
        }

        List<ProductResponse> list = new ArrayList<ProductResponse>( products.size() );
        for ( Product product : products ) {
            list.add( toResponse( product ) );
        }

        return list;
    }

    private Integer productProductTypeSerialId(Product product) {
        if ( product == null ) {
            return null;
        }
        ProductType productType = product.getProductType();
        if ( productType == null ) {
            return null;
        }
        Integer serialId = productType.getSerialId();
        if ( serialId == null ) {
            return null;
        }
        return serialId;
    }

    private String productProductTypeName(Product product) {
        if ( product == null ) {
            return null;
        }
        ProductType productType = product.getProductType();
        if ( productType == null ) {
            return null;
        }
        String name = productType.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
