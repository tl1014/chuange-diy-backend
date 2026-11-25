
package com.chuange.diy.backend.repository;
import com.chuange.diy.backend.model.db.ShopOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ShopOrderItemRepository extends JpaRepository<ShopOrderItem,Long> { List<ShopOrderItem> findByOrder_Id(Long id); }
