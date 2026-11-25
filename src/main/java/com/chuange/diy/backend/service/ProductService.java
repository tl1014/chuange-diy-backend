package com.chuange.diy.backend.service;

import com.chuange.diy.backend.model.db.Category;
import com.chuange.diy.backend.model.db.ElementType;
import com.chuange.diy.backend.model.db.Product;
import com.chuange.diy.backend.model.dto.ProductPageResponse;
import com.chuange.diy.backend.model.dto.ProductRequest;
import com.chuange.diy.backend.model.dto.ProductResponse;
import com.chuange.diy.backend.repository.CategoryRepository;
import com.chuange.diy.backend.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> VALID_DIY_TYPES = Set.of("BEAD", "DIRECTIONAL", "PENDANT");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchEnabled(Long categoryId, String keyword) {
        String q = keyword == null ? "" : keyword.trim();
        List<Product> products;

        if (categoryId == null) {
            products = q.isEmpty()
                    ? productRepository.findByEnabledTrueOrderByIdAsc()
                    : productRepository.findByNameContainingIgnoreCaseAndEnabledTrueOrderByIdAsc(q);
        } else {
            List<Long> categoryIds = categoryService.getCategoryTreeIds(categoryId);
            if (categoryIds.isEmpty()) {
                return new ArrayList<>();
            }
            products = q.isEmpty()
                    ? productRepository.findByCategory_IdInAndEnabledTrueOrderByIdAsc(categoryIds)
                    : productRepository.findByCategory_IdInAndNameContainingIgnoreCaseAndEnabledTrueOrderByIdAsc(categoryIds, q);
        }

        List<ProductResponse> result = new ArrayList<>();
        for (Product p : products) {
            result.add(toResponse(p));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ProductPageResponse searchEnabledPaged(Long categoryId, String keyword, Integer page, Integer size) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size < 1) ? 20 : Math.min(size, 100);

        List<ProductResponse> allMatched = searchEnabled(categoryId, keyword);
        int totalElements = allMatched.size();
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);

        List<ProductResponse> content = new ArrayList<>(allMatched.subList(fromIndex, toIndex));
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        ProductPageResponse pageResponse = new ProductPageResponse();
        pageResponse.content = content;
        pageResponse.page = safePage;
        pageResponse.size = safeSize;
        pageResponse.totalElements = totalElements;
        pageResponse.totalPages = totalPages;
        return pageResponse;
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest form) {
        validateForm(form);
        Product product = new Product();
        applyForm(product, form);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest form) {
        validateForm(form);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));
        applyForm(product, form);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        productRepository.deleteById(id);
    }

    private void validateForm(ProductRequest form) {
        if (form == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品参数不能为空");
        }
        if (form.name == null || form.name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品名称不能为空");
        }
        if (form.categoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择商品分类");
        }
        if (form.price == null || form.price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品价格不能为负数");
        }
        if (form.stock != null && form.stock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品库存不能为负数");
        }
    }

    private void applyForm(Product product, ProductRequest form) {
        Category category = categoryRepository.findById(form.categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品分类不存在"));

        String diyType = (form.diyType == null || form.diyType.isBlank())
                ? "BEAD"
                : form.diyType.trim().toUpperCase(Locale.ROOT);
        if (!VALID_DIY_TYPES.contains(diyType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DIY摆放类型无效");
        }

        product.category = category;
        product.name = form.name.trim();
        product.sku = form.sku == null ? null : form.sku.trim();
        product.imageUrl = form.imageUrl;
        product.diameterMm = form.diameterMm;
        product.diyType = diyType;
        product.widthMm = "BEAD".equals(diyType) ? form.diameterMm : form.widthMm;
        product.heightMm = "BEAD".equals(diyType) ? form.diameterMm : form.heightMm;
        product.price = form.price;
        product.weight = form.weight;
        product.elementType = form.elementType == null ? ElementType.GOLD : form.elementType;
        product.description = form.description;
        product.stock = form.stock == null ? 0 : form.stock;
        product.enabled = form.enabled == null ? Boolean.TRUE : form.enabled;
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse res = new ProductResponse();
        res.id = p.id;
        if (p.category != null) {
            res.categoryId = p.category.id;
            res.categoryName = p.category.name;
            res.categoryCode = p.category.code;
        }
        res.name = p.name;
        res.sku = p.sku;
        res.imageUrl = p.imageUrl;
        res.diameterMm = p.diameterMm;
        res.diyType = p.diyType;
        res.widthMm = p.widthMm;
        res.heightMm = p.heightMm;
        res.price = p.price;
        res.weight = p.weight;
        res.elementType = p.elementType;
        res.description = p.description;
        res.stock = p.stock;
        res.enabled = p.enabled;
        return res;
    }
}