package com.example.order_service.mapper;

import com.example.order_service.dto.reponse.OrderItemResponse;
import com.example.order_service.dto.reponse.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderDetail;
import com.example.order_service.entity.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-09T16:01:03+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.43.0.v20250819-1513, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public OrderResponse toOrderResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse orderResponse = new OrderResponse();

        orderResponse.setSerialId( order.getSerialId() );
        orderResponse.setOrderCode( order.getOrderCode() );
        orderResponse.setUserId( order.getUserId() );
        orderResponse.setCustomerName( order.getCustomerName() );
        orderResponse.setPhoneNumber( order.getPhoneNumber() );
        orderResponse.setShippingAddress( order.getShippingAddress() );
        orderResponse.setEmail( order.getEmail() );
        orderResponse.setOrderNotes( order.getOrderNotes() );
        orderResponse.setTotalAmount( order.getTotalAmount() );
        orderResponse.setStatusId( orderStatusSerialId( order ) );
        orderResponse.setStatusCode( orderStatusStatusCode( order ) );
        orderResponse.setStatusDescription( orderStatusDescription( order ) );
        orderResponse.setPaymentMethod( order.getPaymentMethod() );
        orderResponse.setOrderDate( order.getOrderDate() );
        orderResponse.setUpdatedAt( order.getUpdatedAt() );
        orderResponse.setItems( orderDetailListToOrderItemResponseList( order.getOrderDetails() ) );
        orderResponse.setGuestToken( order.getGuestToken() );

        return orderResponse;
    }

    @Override
    public List<OrderResponse> toOrderResponseList(List<Order> orders) {
        if ( orders == null ) {
            return null;
        }

        List<OrderResponse> list = new ArrayList<OrderResponse>( orders.size() );
        for ( Order order : orders ) {
            list.add( toOrderResponse( order ) );
        }

        return list;
    }

    private Integer orderStatusSerialId(Order order) {
        if ( order == null ) {
            return null;
        }
        OrderStatus status = order.getStatus();
        if ( status == null ) {
            return null;
        }
        Integer serialId = status.getSerialId();
        if ( serialId == null ) {
            return null;
        }
        return serialId;
    }

    private String orderStatusStatusCode(Order order) {
        if ( order == null ) {
            return null;
        }
        OrderStatus status = order.getStatus();
        if ( status == null ) {
            return null;
        }
        String statusCode = status.getStatusCode();
        if ( statusCode == null ) {
            return null;
        }
        return statusCode;
    }

    private String orderStatusDescription(Order order) {
        if ( order == null ) {
            return null;
        }
        OrderStatus status = order.getStatus();
        if ( status == null ) {
            return null;
        }
        String description = status.getDescription();
        if ( description == null ) {
            return null;
        }
        return description;
    }

    protected List<OrderItemResponse> orderDetailListToOrderItemResponseList(List<OrderDetail> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemResponse> list1 = new ArrayList<OrderItemResponse>( list.size() );
        for ( OrderDetail orderDetail : list ) {
            list1.add( orderDetailMapper.toOrderItemResponse( orderDetail ) );
        }

        return list1;
    }
}
