package com.chuange.diy.backend.controller;

import com.chuange.diy.backend.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryView> allEnabled() {
        return categoryService.listEnabled();
    }

    @GetMapping("/admin")
    public List<CategoryView> allForAdmin() {
        return categoryService.listAllForAdmin();
    }

    @PostMapping
    public CategoryView create(@RequestBody CategoryForm form) {
        return categoryService.create(form);
    }

    @PutMapping("/{id}")
    public CategoryView update(@PathVariable Long id, @RequestBody CategoryForm form) {
        return categoryService.update(id, form);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    public record CategoryForm(String name, String code, Integer sortNo, Boolean enabled, Long parentId) {}

    public record CategoryView(Long id, String name, String code, Integer sortNo,
                               Boolean enabled, Long parentId) {}
}