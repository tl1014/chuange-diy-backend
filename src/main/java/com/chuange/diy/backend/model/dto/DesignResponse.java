package com.chuange.diy.backend.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DesignResponse {
    public Long id;
    public Long userId;
    public String userName;
    public String phone;
    public String title;
    public BigDecimal wristCm;
    public BigDecimal totalPrice;
    public String imageUrl;
    public Object items;
    public LocalDateTime createdAt;
}