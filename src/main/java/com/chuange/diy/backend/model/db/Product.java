package com.chuange.diy.backend.model.db;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
public class Product {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 public Long id;

 @ManyToOne(optional = false)
 @JoinColumn(name = "category_id")
 public Category category;

 @Column(nullable = false, length = 120)
 public String name;

 @Column(unique = true, length = 60)
 public String sku;

 @Column(name = "image_url", length = 300)
 public String imageUrl;

 @Column(name = "diameter_mm", precision = 6, scale = 2)
 public BigDecimal diameterMm;

 @Column(name = "diy_type", length = 30)
 public String diyType = "BEAD";

 @Column(name = "width_mm", precision = 6, scale = 2)
 public BigDecimal widthMm;

 @Column(name = "height_mm", precision = 6, scale = 2)
 public BigDecimal heightMm;

 @Column(nullable = false, precision = 10, scale = 2)
 public BigDecimal price;

 @Column(precision = 8, scale = 2)
 public BigDecimal weight;

 @Enumerated(EnumType.STRING)
 @Column(name = "element_type", length = 20)
 public ElementType elementType;

 @Column(length = 500)
 public String description;

 @Column(nullable = false)
 public Integer stock = 0;

 @Column(nullable = false)
 public Boolean enabled = true;
}