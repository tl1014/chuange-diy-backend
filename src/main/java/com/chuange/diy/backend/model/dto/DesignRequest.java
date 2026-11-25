package com.chuange.diy.backend.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class DesignRequest {
    public Long userId;
    public String title;
    public BigDecimal wristCm;
    public List<DesignItemDto> items;
    public String imageUrl;
}