package com.example.product_service.mapper;

import com.example.product_service.dto.response.ProductTypeResponse;
import com.example.product_service.entity.ProductType;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-09T16:01:04+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.43.0.v20250819-1513, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class ProductTypeMapperImpl implements ProductTypeMapper {

    @Override
    public ProductTypeResponse toResponse(ProductType productType) {
        if ( productType == null ) {
            return null;
        }

        ProductTypeResponse productTypeResponse = new ProductTypeResponse();

        productTypeResponse.setName( productType.getName() );
        productTypeResponse.setSerialId( productType.getSerialId() );

        return productTypeResponse;
    }
}
