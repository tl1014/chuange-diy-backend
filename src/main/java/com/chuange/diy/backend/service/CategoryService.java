package com.chuange.diy.backend.service;

import com.chuange.diy.backend.controller.CategoryController.CategoryForm;
import com.chuange.diy.backend.controller.CategoryController.CategoryView;
import com.chuange.diy.backend.model.db.Category;
import com.chuange.diy.backend.repository.CategoryRepository;
import com.chuange.diy.backend.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class CategoryService {

    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Za-z0-9_-]{2,60}");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryView> listEnabled() {
        return categoryRepository.findAllByOrderBySortNoAscIdAsc().stream()
                .filter(c -> Boolean.TRUE.equals(c.enabled))
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryView> listAllForAdmin() {
        return categoryRepository.findAllByOrderBySortNoAscIdAsc().stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public CategoryView create(CategoryForm form) {
        validate(form, null);
        Category category = new Category();
        applyForm(category, form);
        return toView(categoryRepository.save(category));
    }

    @Transactional
    public CategoryView update(Long id, CategoryForm form) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));
        validate(form, id);
        applyForm(category, form);
        return toView(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));

        boolean hasChildren = categoryRepository.findAll().stream()
                .anyMatch(item -> item.parent != null && Objects.equals(item.parent.id, id));
        if (hasChildren) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该分类下还有子分类，请先删除或移动子分类");
        }
        if (productRepository.existsByCategory_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该分类下仍有商品，请先移动或删除商品");
        }
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<Long> getCategoryTreeIds(Long rootId) {
        if (rootId == null || !categoryRepository.existsById(rootId)) {
            return List.of();
        }
        List<Category> allCategories = categoryRepository.findAll();
        Set<Long> result = new LinkedHashSet<>();
        result.add(rootId);

        boolean changed;
        do {
            changed = false;
            for (Category category : allCategories) {
                if (category.parent != null
                        && result.contains(category.parent.id)
                        && result.add(category.id)) {
                    changed = true;
                }
            }
        } while (changed);

        return new ArrayList<>(result);
    }

    private void validate(CategoryForm form, Long currentId) {
        if (form == null || form.name() == null || form.name().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分类名称不能为空");
        }
        if (form.name().trim().length() > 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分类名称不能超过60个字符");
        }
        if (form.code() == null || !CODE_PATTERN.matcher(form.code().trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分类编码需为2-60位字母、数字、下划线或短横线");
        }

        String normalizedCode = form.code().trim();
        boolean duplicate = categoryRepository.findAll().stream().anyMatch(item ->
                item.code != null
                        && item.code.equalsIgnoreCase(normalizedCode)
                        && !Objects.equals(item.id, currentId));
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "分类编码已经存在");
        }

        if (form.parentId() != null) {
            if (Objects.equals(form.parentId(), currentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能将分类设置为自己的父级");
            }
            Category parent = categoryRepository.findById(form.parentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "所选父级分类不存在"));

            Set<Long> visited = new HashSet<>();
            Category cursor = parent;
            while (cursor != null) {
                if (!visited.add(cursor.id)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "现有分类层级存在循环，请先修复");
                }
                if (Objects.equals(cursor.id, currentId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能将自己的下级分类设为父级");
                }
                cursor = cursor.parent;
            }
        }
    }

    private void applyForm(Category category, CategoryForm form) {
        category.name = form.name().trim();
        category.code = form.code().trim();
        category.sortNo = form.sortNo() == null ? 0 : form.sortNo();
        category.enabled = form.enabled() == null ? Boolean.TRUE : form.enabled();
        category.parent = form.parentId() == null
                ? null
                : categoryRepository.findById(form.parentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "父级分类不存在"));
    }

    private CategoryView toView(Category category) {
        return new CategoryView(
                category.id,
                category.name,
                category.code,
                category.sortNo == null ? 0 : category.sortNo,
                category.enabled == null || category.enabled,
                category.parent == null ? null : category.parent.id
        );
    }
}