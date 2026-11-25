package com.chuange.diy.backend.model.db;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shop_order_item")
public class ShopOrderItem {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 public Long id;

 @ManyToOne(optional = false)
 @JoinColumn(name = "order_id")
 public ShopOrder order;

 @Column(name = "product_id", nullable = false)
 public Long productId;

 @Column(name = "product_name", nullable = false, length = 120)
 public String productName;

 @Column(nullable = false)
 public Integer quantity;

 @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
 public BigDecimal unitPrice;

 @Column(name = "diameter_mm", precision = 6, scale = 2)
 public BigDecimal diameterMm;
}