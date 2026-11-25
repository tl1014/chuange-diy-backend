package com.chuange.diy.backend.spring_config;

import com.chuange.diy.backend.model.db.Category;
import com.chuange.diy.backend.model.db.DiyDesign;
import com.chuange.diy.backend.model.db.ElementType;
import com.chuange.diy.backend.model.db.Product;
import com.chuange.diy.backend.model.db.ShopOrder;
import com.chuange.diy.backend.model.db.ShopOrderItem;
import com.chuange.diy.backend.model.db.ShopUser;
import com.chuange.diy.backend.repository.CategoryRepository;
import com.chuange.diy.backend.repository.DiyDesignRepository;
import com.chuange.diy.backend.repository.ProductRepository;
import com.chuange.diy.backend.repository.ShopOrderItemRepository;
import com.chuange.diy.backend.repository.ShopOrderRepository;
import com.chuange.diy.backend.repository.ShopUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TestDataGenerator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TestDataGenerator.class);

    public static final String DEV_ADMIN_USERNAME = "seed-admin";
    public static final String DEV_OMNI_TOKEN = "dev-omni-admin-token-2026";

    @Value("${app.generate-data.enabled:false}")
    private boolean generateDataEnabled;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ShopUserRepository shopUserRepository;
    private final DiyDesignRepository diyDesignRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final ShopOrderItemRepository shopOrderItemRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    public TestDataGenerator(CategoryRepository categoryRepository,
                             ProductRepository productRepository,
                             ShopUserRepository shopUserRepository,
                             DiyDesignRepository diyDesignRepository,
                             ShopOrderRepository shopOrderRepository,
                             ShopOrderItemRepository shopOrderItemRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.shopUserRepository = shopUserRepository;
        this.diyDesignRepository = diyDesignRepository;
        this.shopOrderRepository = shopOrderRepository;
        this.shopOrderItemRepository = shopOrderItemRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!generateDataEnabled) {
            return;
        }
        log.info("app.generate-data.enabled=true, starting idempotent CRUD data generation...");

        ShopUser adminUser = ensureAdminUser();

        Category beadRoot = ensureCategory("珠子", "BEAD", 0, null);
        Category quartzSub = ensureCategory("白水晶", "WHITE_QUARTZ", 1, beadRoot);
        Category obsidianSub = ensureCategory("曜石", "OBSIDIAN", 2, beadRoot);
        Category pendantRoot = ensureCategory("吊坠", "PENDANT", 3, null);

        Product p1 = ensureProduct(
                quartzSub, "奶白晶", "SEED-BEAD-001", "/uploads/BEAD-0001.png",
                new BigDecimal("8.00"), "BEAD", new BigDecimal("8.00"), new BigDecimal("8.00"),
                new BigDecimal("3.00"), new BigDecimal("0.70"), ElementType.GOLD,
                "测试商品：8mm 奶白晶", 100
        );
        Product p2 = ensureProduct(
                obsidianSub, "黑曜石", "SEED-BEAD-002", "/uploads/BEAD-0018.png",
                new BigDecimal("6.00"), "BEAD", new BigDecimal("6.00"), new BigDecimal("6.00"),
                new BigDecimal("1.00"), new BigDecimal("0.20"), ElementType.WATER,
                "测试商品：6mm 黑曜石", 100
        );
        ensureProduct(
                pendantRoot, "摩卡色白色鱼尾吊坠", "SEED-PENDANT-001", "/uploads/PENDANT-0001.png",
                new BigDecimal("0.10"), "PENDANT", null, null,
                new BigDecimal("1.50"), new BigDecimal("1.00"), ElementType.GOLD,
                "测试商品：鱼尾吊坠", 50
        );

        ShopUser demoUser = shopUserRepository.findAll().stream()
                .filter(u -> "seed-guest".equals(u.username))
                .findFirst()
                .orElseGet(() -> {
                    ShopUser u = new ShopUser();
                    u.username = "seed-guest";
                    u.role = "USER";
                    u.name = "测试访客";
                    u.phone = "13800000000";
                    u.address = "贵阳市观山湖区测试地址";
                    return shopUserRepository.save(u);
                });

        DiyDesign design = diyDesignRepository.findAll().stream()
                .filter(d -> "SEED-DESIGN-001".equals(d.title))
                .findFirst()
                .orElseGet(() -> {
                    DiyDesign d = new DiyDesign();
                    d.user = demoUser;
                    d.title = "SEED-DESIGN-001";
                    d.wristCm = new BigDecimal("16.00");
                    d.totalPrice = new BigDecimal("10.00");
                    d.designJson = "[{\"productId\":" + p1.id + ",\"quantity\":2},{\"productId\":" + p2.id + ",\"quantity\":4}]";
                    return diyDesignRepository.save(d);
                });

        if (shopOrderRepository.findAll().stream().noneMatch(o -> "CG-SEED-0001".equals(o.orderNo))) {
            ShopOrder order = new ShopOrder();
            order.orderNo = "CG-SEED-0001";
            order.user = adminUser;
            order.design = design;
            order.totalPrice = new BigDecimal("10.00");
            order.status = "PENDING";
            order.paymentMethod = "SIMULATED";
            order.address = adminUser.address;
            ShopOrder savedOrder = shopOrderRepository.save(order);

            ShopOrderItem item1 = new ShopOrderItem();
            item1.order = savedOrder;
            item1.productId = p1.id;
            item1.productName = p1.name;
            item1.quantity = 2;
            item1.unitPrice = p1.price;
            item1.diameterMm = p1.diameterMm;

            ShopOrderItem item2 = new ShopOrderItem();
            item2.order = savedOrder;
            item2.productId = p2.id;
            item2.productName = p2.name;
            item2.quantity = 4;
            item2.unitPrice = p2.price;
            item2.diameterMm = p2.diameterMm;

            shopOrderItemRepository.saveAll(List.of(item1, item2));
        }

        log.info("Test data generation completed. Dev omni token: {}", DEV_OMNI_TOKEN);
    }

    private ShopUser ensureAdminUser() {
        return shopUserRepository.findAll().stream()
                .filter(u -> DEV_ADMIN_USERNAME.equals(u.username))
                .findFirst()
                .map(existing -> {
                    existing.role = "ADMIN";
                    return shopUserRepository.save(existing);
                })
                .orElseGet(() -> {
                    ShopUser admin = new ShopUser();
                    admin.username = DEV_ADMIN_USERNAME;
                    admin.email = "admin@chuange-diy.local";
                    admin.passwordHash = passwordEncoder != null
                            ? passwordEncoder.encode("admin123456")
                            : "admin123456";
                    admin.role = "ADMIN";
                    admin.name = "开发全能管理员";
                    admin.phone = "13900000000";
                    admin.address = "贵阳市观山湖区测试中心";
                    return shopUserRepository.save(admin);
                });
    }

    private Category ensureCategory(String name, String code, int sortNo, Category parent) {
        return categoryRepository.findAll().stream()
                .filter(c -> code.equalsIgnoreCase(c.code))
                .findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.name = name;
                    c.code = code;
                    c.sortNo = sortNo;
                    c.enabled = true;
                    c.parent = parent;
                    return categoryRepository.save(c);
                });
    }

    private Product ensureProduct(Category category,
                                  String name,
                                  String sku,
                                  String imageUrl,
                                  BigDecimal diameterMm,
                                  String diyType,
                                  BigDecimal widthMm,
                                  BigDecimal heightMm,
                                  BigDecimal price,
                                  BigDecimal weight,
                                  ElementType elementType,
                                  String description,
                                  int stock) {
        return productRepository.findAll().stream()
                .filter(p -> sku.equalsIgnoreCase(p.sku))
                .findFirst()
                .orElseGet(() -> {
                    Product p = new Product();
                    p.category = category;
                    p.name = name;
                    p.sku = sku;
                    p.imageUrl = imageUrl;
                    p.diameterMm = diameterMm;
                    p.diyType = diyType;
                    p.widthMm = widthMm;
                    p.heightMm = heightMm;
                    p.price = price;
                    p.weight = weight;
                    p.elementType = elementType;
                    p.description = description;
                    p.stock = stock;
                    p.enabled = true;
                    return productRepository.save(p);
                });
    }
}