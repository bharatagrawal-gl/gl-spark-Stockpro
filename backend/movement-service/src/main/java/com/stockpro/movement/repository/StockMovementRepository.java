package com.stockpro.movement.repository;

import com.stockpro.movement.entity.MovementType;
import com.stockpro.movement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByWarehouseId(Long warehouseId);
    List<StockMovement> findByProductId(Long productId);
    List<StockMovement> findByMovementType(MovementType movementType);
    List<StockMovement> findByPerformedBy(Long userId);
    List<StockMovement> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);
    List<StockMovement> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    List<StockMovement> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}