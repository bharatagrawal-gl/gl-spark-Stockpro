package com.stockpro.warehouse.repository;

import com.stockpro.warehouse.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {
    Optional<StockLevel> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
    List<StockLevel> findByWarehouseId(Long warehouseId);
    List<StockLevel> findByProductId(Long productId);
}