package com.chuange.diy.backend.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {
    public Long userId;
    public String name;
    public String phone;
    public String address;
    public Long designId;
    public List<OrderItemRequest> items;
    public BigDecimal wristCm;
    public List<DesignItemDto> designItems;
    public String designImageUrl;
}