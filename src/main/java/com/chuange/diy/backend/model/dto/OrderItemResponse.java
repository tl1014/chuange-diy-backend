package com.chuange.diy.backend.model.dto;

import java.math.BigDecimal;

public class OrderItemResponse {
    public Long productId;
    public String productName;
    public Integer quantity;
    public BigDecimal unitPrice;
    public BigDecimal diameterMm;
}