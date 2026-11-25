package com.chuange.diy.backend.repository;

import com.chuange.diy.backend.model.db.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCode(String code);

    List<Category> findAllByOrderBySortNoAscIdAsc();
}