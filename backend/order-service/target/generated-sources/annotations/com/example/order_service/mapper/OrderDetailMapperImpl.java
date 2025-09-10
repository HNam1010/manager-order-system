package com.example.order_service.mapper;

import com.example.order_service.dto.reponse.OrderItemResponse;
import com.example.order_service.entity.OrderDetail;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-09T16:01:02+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.43.0.v20250819-1513, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class OrderDetailMapperImpl implements OrderDetailMapper {

    @Override
    public OrderItemResponse toOrderItemResponse(OrderDetail orderDetail) {
        if ( orderDetail == null ) {
            return null;
        }

        OrderItemResponse orderItemResponse = new OrderItemResponse();

        orderItemResponse.setOrderDetailId( orderDetail.getOrderDetailId() );
        orderItemResponse.setProductId( orderDetail.getProductId() );
        orderItemResponse.setProductName( orderDetail.getProductName() );
        orderItemResponse.setProductBrand( orderDetail.getProductBrand() );
        orderItemResponse.setProductImagePath( orderDetail.getProductImagePath() );
        orderItemResponse.setProductPrice( orderDetail.getProductPrice() );
        orderItemResponse.setQuantity( orderDetail.getQuantity() );
        orderItemResponse.setTotalPrice( orderDetail.getTotalPrice() );

        return orderItemResponse;
    }
}
