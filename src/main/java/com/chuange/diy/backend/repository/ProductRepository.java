package com.chuange.diy.backend.repository;

import com.chuange.diy.backend.model.db.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByEnabledTrueOrderByIdAsc();

    List<Product> findByCategory_IdAndEnabledTrueOrderByIdAsc(Long categoryId);

    List<Product> findByCategory_IdInAndEnabledTrueOrderByIdAsc(List<Long> categoryIds);

    List<Product> findByNameContainingIgnoreCaseAndEnabledTrueOrderByIdAsc(String keyword);

    List<Product> findByCategory_IdInAndNameContainingIgnoreCaseAndEnabledTrueOrderByIdAsc(
            List<Long> categoryIds, String keyword);

    boolean existsByCategory_Id(Long categoryId);
}
