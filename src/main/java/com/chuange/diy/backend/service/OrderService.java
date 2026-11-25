package com.chuange.diy.backend.service;

import com.chuange.diy.backend.model.db.DiyDesign;
import com.chuange.diy.backend.model.db.Product;
import com.chuange.diy.backend.model.db.ShopOrder;
import com.chuange.diy.backend.model.db.ShopOrderItem;
import com.chuange.diy.backend.model.db.ShopUser;
import com.chuange.diy.backend.model.dto.OrderItemRequest;
import com.chuange.diy.backend.model.dto.OrderItemResponse;
import com.chuange.diy.backend.model.dto.OrderRequest;
import com.chuange.diy.backend.model.dto.OrderResponse;
import com.chuange.diy.backend.repository.DiyDesignRepository;
import com.chuange.diy.backend.repository.ProductRepository;
import com.chuange.diy.backend.repository.ShopOrderItemRepository;
import com.chuange.diy.backend.repository.ShopOrderRepository;
import com.chuange.diy.backend.repository.ShopUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final ShopOrderRepository orderRepository;
    private final ShopOrderItemRepository orderItemRepository;
    private final ShopUserRepository userRepository;
    private final ProductRepository productRepository;
    private final DiyDesignRepository designRepository;
    private final DesignService designService;

    public OrderService(ShopOrderRepository orderRepository,
                        ShopOrderItemRepository orderItemRepository,
                        ShopUserRepository userRepository,
                        ProductRepository productRepository,
                        DiyDesignRepository designRepository,
                        DesignService designService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.designRepository = designRepository;
        this.designService = designService;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (request == null || isBlank(request.name) || isBlank(request.phone) || isBlank(request.address)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请完整填写收货人、手机号和收货地址");
        }
        if (request.items == null || request.items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单商品不能为空");
        }

        Map<Long, Integer> grouped = new LinkedHashMap<>();
        for (OrderItemRequest line : request.items) {
            if (line == null || line.productId == null || line.quantity == null
                    || line.quantity < 1 || line.quantity > 999) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品数量格式不正确，请确认数量为正整数");
            }
            int qty = grouped.getOrDefault(line.productId, 0) + line.quantity;
            if (qty > 999) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "单个商品数量不能超过999");
            }
            grouped.put(line.productId, qty);
        }

        ShopUser user = resolveCustomer(request);

        ShopOrder order = new ShopOrder();
        order.user = user;
        order.address = request.address.trim();
        order.orderNo = generateUniqueOrderNo();
        order.totalPrice = BigDecimal.ZERO;
        order.status = "PENDING";
        if (request.designId != null) {
            order.design = designRepository.findById(request.designId).orElse(null);
        }
        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<ShopOrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : grouped.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品不存在"));
            if (!Boolean.TRUE.equals(product.enabled)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品已下架：" + product.name);
            }
            if (product.stock == null || product.stock < entry.getValue()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品库存不足：" + product.name);
            }

            ShopOrderItem item = new ShopOrderItem();
            item.order = order;
            item.productId = product.id;
            item.productName = product.name;
            item.quantity = entry.getValue();
            item.unitPrice = product.price;
            item.diameterMm = product.diameterMm;
            orderItems.add(item);

            total = total.add(product.price.multiply(BigDecimal.valueOf(entry.getValue())));
        }
        orderItemRepository.saveAll(orderItems);

        order.totalPrice = total;

        DiyDesign snapshotDesign = new DiyDesign();
        snapshotDesign.user = user;
        snapshotDesign.title = "订单DIY方案 " + order.orderNo;
        snapshotDesign.wristCm = request.wristCm == null ? new BigDecimal("16") : request.wristCm;
        snapshotDesign.totalPrice = total;
        snapshotDesign.imageUrl = request.designImageUrl;
        Object rawDesignItems = (request.designItems == null || request.designItems.isEmpty())
                ? request.items
                : request.designItems;
        snapshotDesign.designJson = designService.writeJson(rawDesignItems);
        snapshotDesign = designRepository.save(snapshotDesign);

        order.design = snapshotDesign;
        order = orderRepository.save(order);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        ShopOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listAllForAdmin() {
        List<ShopOrder> all = new ArrayList<>(orderRepository.findAll());
        all.sort(Comparator.comparing((ShopOrder o) -> o.id).reversed());

        List<OrderResponse> result = new ArrayList<>();
        for (ShopOrder order : all) {
            result.add(toResponse(order));
        }
        return result;
    }

    @Transactional
    public OrderResponse payOrder(Long id) {
        ShopOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));
        if ("PAID".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该订单已经支付");
        }
        if (!"PENDING".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前订单状态不可支付");
        }

        List<ShopOrderItem> lines = orderItemRepository.findByOrder_Id(id);
        for (ShopOrderItem line : lines) {
            Product product = productRepository.findById(line.productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品不存在：" + line.productName));
            int currentStock = product.stock == null ? 0 : product.stock;
            if (currentStock < line.quantity) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "库存不足：" + line.productName);
            }
            product.stock = currentStock - line.quantity;
            productRepository.save(product);
        }

        order.status = "PAID";
        order.paymentMethod = "SIMULATED";
        order.paidAt = LocalDateTime.now();
        order = orderRepository.save(order);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        ShopOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));
        if ("PAID".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已支付订单不能直接取消，请走退款流程");
        }
        order.status = "CANCELLED";
        order = orderRepository.save(order);
        return toResponse(order);
    }

    private ShopUser resolveCustomer(OrderRequest request) {
        if (request.userId != null) {
            ShopUser existing = userRepository.findById(request.userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "关联用户不存在"));
            existing.name = request.name.trim();
            existing.phone = request.phone.trim();
            existing.address = request.address.trim();
            return userRepository.save(existing);
        }
        ShopUser customer = new ShopUser();
        customer.name = request.name.trim();
        customer.phone = request.phone.trim();
        customer.address = request.address.trim();
        return userRepository.save(customer);
    }

    private String generateUniqueOrderNo() {
        String orderNo;
        do {
            orderNo = "CG" + System.currentTimeMillis()
                    + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
        } while (orderRepository.existsByOrderNo(orderNo));
        return orderNo;
    }

    private OrderResponse toResponse(ShopOrder order) {
        OrderResponse res = new OrderResponse();
        res.id = order.id;
        res.orderNo = order.orderNo;
        res.userId = order.user != null ? order.user.id : null;
        res.name = order.user != null ? order.user.name : "";
        res.phone = order.user != null ? order.user.phone : "";
        res.address = order.address;
        res.totalPrice = order.totalPrice;
        res.status = order.status;
        res.paymentMethod = order.paymentMethod;
        res.paidAt = order.paidAt;
        res.createdAt = order.createdAt;
        res.designImageUrl = order.design == null ? null : order.design.imageUrl;

        List<ShopOrderItem> lines = orderItemRepository.findByOrder_Id(order.id);
        List<OrderItemResponse> itemList = new ArrayList<>();
        for (ShopOrderItem x : lines) {
            OrderItemResponse itemRes = new OrderItemResponse();
            itemRes.productId = x.productId;
            itemRes.productName = x.productName;
            itemRes.quantity = x.quantity;
            itemRes.unitPrice = x.unitPrice;
            itemRes.diameterMm = x.diameterMm == null ? BigDecimal.ZERO : x.diameterMm;
            itemList.add(itemRes);
        }
        res.items = itemList;
        return res;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}