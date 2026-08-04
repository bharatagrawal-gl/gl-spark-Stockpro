package com.stockpro.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_levels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime updatedAt=LocalDateTime.now();;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}