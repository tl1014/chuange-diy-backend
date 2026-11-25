
package com.chuange.diy.backend.repository;
import com.chuange.diy.backend.model.db.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ShopOrderRepository extends JpaRepository<ShopOrder,Long> { boolean existsByOrderNo(String orderNo); }
