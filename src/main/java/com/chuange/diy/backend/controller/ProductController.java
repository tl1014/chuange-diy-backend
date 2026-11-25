package com.chuange.diy.backend.controller;

import com.chuange.diy.backend.model.dto.ProductRequest;
import com.chuange.diy.backend.model.dto.ProductResponse;
import com.chuange.diy.backend.service.ProductService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> all(@RequestParam(required = false) Long categoryId,
                                     @RequestParam(required = false) String keyword) {
        return productService.searchEnabled(categoryId, keyword);
    }

    @GetMapping("/paged")
    public com.chuange.diy.backend.model.dto.ProductPageResponse paged(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return productService.searchEnabledPaged(categoryId, keyword, page, size);
    }

    @GetMapping("/{id}")
    public ProductResponse one(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PostMapping
    public ProductResponse create(@RequestBody ProductRequest form) {
        return productService.create(form);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody ProductRequest form) {
        return productService.update(id, form);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}