package com.chuange.diy.backend.service;

import com.chuange.diy.backend.model.db.DiyDesign;
import com.chuange.diy.backend.model.db.Product;
import com.chuange.diy.backend.model.db.ShopUser;
import com.chuange.diy.backend.model.dto.DesignItemDto;
import com.chuange.diy.backend.model.dto.DesignRequest;
import com.chuange.diy.backend.model.dto.DesignResponse;
import com.chuange.diy.backend.repository.DiyDesignRepository;
import com.chuange.diy.backend.repository.ProductRepository;
import com.chuange.diy.backend.repository.ShopUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class DesignService {

    private final DiyDesignRepository designRepository;
    private final ShopUserRepository userRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public DesignService(DiyDesignRepository designRepository,
                         ShopUserRepository userRepository,
                         ProductRepository productRepository,
                         ObjectMapper objectMapper) {
        this.designRepository = designRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DesignResponse saveDesign(DesignRequest request) {
        if (request == null || request.userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少用户信息");
        }
        ShopUser user = userRepository.findById(request.userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (request.items == null || request.items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DIY方案不能为空");
        }

        DiyDesign design = new DiyDesign();
        design.user = user;
        design.title = (request.title == null || request.title.isBlank()) ? "我的手串方案" : request.title.trim();
        design.wristCm = request.wristCm == null ? new BigDecimal("16") : request.wristCm;
        design.totalPrice = calculateTotal(request.items);
        design.designJson = writeJson(request.items);
        design.imageUrl = request.imageUrl;

        DiyDesign saved = designRepository.save(design);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DesignResponse> listByUserId(Long userId) {
        List<DiyDesign> all = designRepository.findAll();
        List<DiyDesign> matched = new ArrayList<>();
        for (DiyDesign d : all) {
            if (d.user != null && Objects.equals(d.user.id, userId)) {
                matched.add(d);
            }
        }
        matched.sort(Comparator.comparing((DiyDesign d) -> d.id).reversed());

        List<DesignResponse> result = new ArrayList<>();
        for (DiyDesign d : matched) {
            result.add(toResponse(d));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<DesignResponse> listAllForAdmin() {
        List<DiyDesign> all = new ArrayList<>(designRepository.findAll());
        all.sort(Comparator.comparing((DiyDesign d) -> d.id).reversed());

        List<DesignResponse> result = new ArrayList<>();
        for (DiyDesign d : all) {
            result.add(toResponse(d));
        }
        return result;
    }

    public BigDecimal calculateTotal(List<DesignItemDto> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (DesignItemDto item : items) {
            if (item == null || item.productId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DIY商品数据缺少商品编号");
            }
            int quantity = item.quantity == null ? 1 : item.quantity;
            if (quantity < 1 || quantity > 999) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品数量格式不正确，请确认数量为正整数");
            }
            Product product = productRepository.findById(item.productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品不存在"));
            sum = sum.add(product.price.multiply(BigDecimal.valueOf(quantity)));
        }
        return sum;
    }

    public String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DIY数据序列化失败");
        }
    }

    public Object readJson(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private DesignResponse toResponse(DiyDesign d) {
        DesignResponse res = new DesignResponse();
        res.id = d.id;
        res.userId = d.user != null ? d.user.id : null;
        res.userName = d.user != null ? d.user.name : "";
        res.phone = d.user != null ? d.user.phone : "";
        res.title = d.title;
        res.wristCm = d.wristCm;
        res.totalPrice = d.totalPrice;
        res.imageUrl = d.imageUrl;
        res.items = readJson(d.designJson);
        res.createdAt = d.createdAt;
        return res;
    }
}