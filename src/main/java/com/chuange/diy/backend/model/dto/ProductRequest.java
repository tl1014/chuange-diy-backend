package com.chuange.diy.backend.model.dto;

import com.chuange.diy.backend.model.db.ElementType;

import java.math.BigDecimal;

public class ProductRequest {
    public Long categoryId;
    public String name;
    public String sku;
    public String imageUrl;
    public BigDecimal diameterMm;
    public BigDecimal price;
    public BigDecimal weight;
    public ElementType elementType;
    public String description;
    public Integer stock;
    public Boolean enabled;
    public String diyType;
    public BigDecimal widthMm;
    public BigDecimal heightMm;
}