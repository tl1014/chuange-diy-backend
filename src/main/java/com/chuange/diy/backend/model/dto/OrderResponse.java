package com.chuange.diy.backend.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    public Long id;
    public String orderNo;
    public Long userId;
    public String name;
    public String phone;
    public String address;
    public BigDecimal totalPrice;
    public String status;
    public String paymentMethod;
    public LocalDateTime paidAt;
    public LocalDateTime createdAt;
    public String designImageUrl;
    public List<OrderItemResponse> items;
}