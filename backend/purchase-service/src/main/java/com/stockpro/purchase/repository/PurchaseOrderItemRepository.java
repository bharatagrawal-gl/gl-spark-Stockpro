package com.stockpro.purchase.repository;

import com.stockpro.purchase.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);
    List<PurchaseOrderItem> findByProductId(Long productId);
}