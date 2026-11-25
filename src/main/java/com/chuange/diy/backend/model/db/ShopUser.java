package com.chuange.diy.backend.model.db;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_user")
public class ShopUser {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 public Long id;

 @Column(unique = true, length = 60)
 public String username;

 @Column(unique = true, length = 120)
 public String email;

 @Column(name = "password_hash", length = 120)
 public String passwordHash;

 @Column(nullable = false, length = 20)
 public String role = "USER";

 @Column(nullable = false, length = 80)
 public String name;

 @Column(nullable = false, length = 30)
 public String phone;

 @Column(length = 300)
 public String address;

 @Column(name = "created_at", nullable = false, updatable = false)
 public LocalDateTime createdAt;

 @PrePersist
 protected void onCreate() {
  if (createdAt == null) {
   createdAt = LocalDateTime.now();
  }
  if (role == null || role.isBlank()) {
   role = "USER";
  }
 }
}