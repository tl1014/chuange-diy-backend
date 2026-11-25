package com.chuange.diy.backend.model.db;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_order")
public class ShopOrder {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 public Long id;

 @Column(name = "order_no", nullable = false, unique = true, length = 40)
 public String orderNo;

 @ManyToOne(optional = false)
 @JoinColumn(name = "user_id")
 public ShopUser user;

 @ManyToOne
 @JoinColumn(name = "design_id")
 public DiyDesign design;

 @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
 public BigDecimal totalPrice;

 @Column(nullable = false, length = 20)
 public String status = "PENDING";

 @Column(name = "payment_method", length = 30)
 public String paymentMethod = "SIMULATED";

 @Column(name = "paid_at")
 public LocalDateTime paidAt;

 @Column(length = 300)
 public String address;

 @Column(name = "created_at", nullable = false, updatable = false)
 public LocalDateTime createdAt;

 @PrePersist
 protected void onCreate() {
  if (createdAt == null) {
   createdAt = LocalDateTime.now();
  }
 }
}