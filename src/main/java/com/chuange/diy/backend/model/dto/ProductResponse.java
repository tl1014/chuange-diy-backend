package com.chuange.diy.backend.model.dto;

import com.chuange.diy.backend.model.db.ElementType;

import java.math.BigDecimal;

public class ProductResponse {
    public Long id;
    public Long categoryId;
    public String categoryName;
    public String categoryCode;
    public String name;
    public String sku;
    public String imageUrl;
    public BigDecimal diameterMm;
    public String diyType;
    public BigDecimal widthMm;
    public BigDecimal heightMm;
    public BigDecimal price;
    public BigDecimal weight;
    public ElementType elementType;
    public String description;
    public Integer stock;
    public Boolean enabled;
}