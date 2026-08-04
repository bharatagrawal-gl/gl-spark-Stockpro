package com.stockpro.warehouse.event;

import com.stockpro.warehouse.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${low.stock.threshold:10}")
    private Integer lowStockThreshold;

    public void publishIfLowStock(Long warehouseId, Long productId, Integer currentQuantity) {
        if (currentQuantity <= lowStockThreshold) {
            LowStockEvent event = LowStockEvent.builder()
                    .warehouseId(warehouseId)
                    .productId(productId)
                    .currentQuantity(currentQuantity)
                    .threshold(lowStockThreshold)
                    .occurredAt(LocalDateTime.now())
                    .build();
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.RK_LOWSTOCK,
                        event
                );
                log.info("LOW_STOCK event published: productId={}, warehouseId={}, qty={}",
                        productId, warehouseId, currentQuantity);
            } catch (Exception e) {
                // Best-effort — never fail stock operation because of publish failure
                log.error("Failed to publish LOW_STOCK event: {}", e.getMessage());
            }
        }
    }
}