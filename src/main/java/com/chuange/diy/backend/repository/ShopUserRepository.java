package com.chuange.diy.backend.repository;

import com.chuange.diy.backend.model.db.ShopUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopUserRepository extends JpaRepository<ShopUser, Long> {
    Optional<ShopUser> findByUsername(String username);
    Optional<ShopUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}