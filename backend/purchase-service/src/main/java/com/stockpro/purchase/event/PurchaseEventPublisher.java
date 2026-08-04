package com.stockpro.purchase.event;

import com.stockpro.purchase.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPoOverdue(Long poId, Long supplierId,
                                 java.time.LocalDate expectedDate) {
        PoOverdueEvent event = PoOverdueEvent.builder()
                .purchaseOrderId(poId)
                .supplierId(supplierId)
                .expectedDate(expectedDate)
                .occurredAt(LocalDateTime.now())
                .build();
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_OVERDUE,
                    event
            );
            log.info("PO_OVERDUE event published: poId={}", poId);
        } catch (Exception e) {
            log.error("Failed to publish PO_OVERDUE event: {}", e.getMessage());
        }
    }
}