package com.chuange.diy.backend.model.dto;

import java.util.List;

public class ProductPageResponse {
    public List<ProductResponse> content;
    public Integer page;
    public Integer size;
    public Integer totalElements;
    public Integer totalPages;
}