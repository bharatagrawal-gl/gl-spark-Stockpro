package com.stockpro.alert.listener;

import com.stockpro.alert.event.LowStockEvent;
import com.stockpro.alert.event.PoOverdueEvent;
import com.stockpro.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertListener {

    private final AlertService alertService;

    @RabbitListener(queues = "alert.lowstock")
    public void handleLowStock(LowStockEvent event) {
        log.info("LOW_STOCK event received: productId={}, warehouseId={}, qty={}",
                event.getProductId(), event.getWarehouseId(), event.getCurrentQuantity());
        try {
            String message = String.format(
                    "Low stock alert: Product ID %d in Warehouse ID %d has only %d units " +
                            "(threshold: %d). Reorder recommended.",
                    event.getProductId(), event.getWarehouseId(),
                    event.getCurrentQuantity(), event.getThreshold()
            );
            alertService.createAlert(
                    "LOW_STOCK",
                    event.getProductId(),
                    "PRODUCT",
                    message
            );
        } catch (Exception e) {
            log.error("Failed to process LOW_STOCK event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "alert.po.overdue")
    public void handlePoOverdue(PoOverdueEvent event) {
        log.info("PO_OVERDUE event received: poId={}, supplierId={}",
                event.getPurchaseOrderId(), event.getSupplierId());
        try {
            String message = String.format(
                    "Purchase Order ID %d from Supplier ID %d is overdue. " +
                            "Expected by: %s. Follow up with supplier.",
                    event.getPurchaseOrderId(), event.getSupplierId(),
                    event.getExpectedDate()
            );
            alertService.createAlert(
                    "PO_OVERDUE",
                    event.getPurchaseOrderId(),
                    "PURCHASE_ORDER",
                    message
            );
        } catch (Exception e) {
            log.error("Failed to process PO_OVERDUE event: {}", e.getMessage());
        }
    }
}