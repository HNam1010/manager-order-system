package com.example.order_service.service.serviceimpl;

import com.example.order_service.entity.Order;
import org.springframework.context.ApplicationEvent;

public class OrderConfirmedEvent extends ApplicationEvent {
    private final Order order; // Giữ Order object


    public OrderConfirmedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    public Order getOrder() { return order; }

}