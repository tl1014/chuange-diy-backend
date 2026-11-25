package com.chuange.diy.backend.model.db;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "diy_design")
public class DiyDesign {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 public Long id;

 @ManyToOne(optional = false)
 @JoinColumn(name = "user_id")
 public ShopUser user;

 @Column(nullable = false, length = 100)
 public String title;

 @Column(name = "wrist_cm", precision = 5, scale = 2)
 public BigDecimal wristCm;

 @Column(name = "total_price", precision = 10, scale = 2)
 public BigDecimal totalPrice;

 @Column(name = "image_url", length = 500)
 public String imageUrl;

 @Column(name = "design_json", nullable = false, columnDefinition = "text")
 public String designJson;

 @Column(name = "created_at", nullable = false, updatable = false)
 public LocalDateTime createdAt;

 @PrePersist
 protected void onCreate() {
  if (createdAt == null) {
   createdAt = LocalDateTime.now();
  }
 }
}