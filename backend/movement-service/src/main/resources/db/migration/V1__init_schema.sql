CREATE TABLE IF NOT EXISTS stock_movements (
                                               id BIGSERIAL PRIMARY KEY,
                                               warehouse_id BIGINT NOT NULL,
                                               product_id BIGINT NOT NULL,

                                               movement_type VARCHAR(20) NOT NULL,

    quantity INT NOT NULL,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    notes TEXT,

    performed_by BIGINT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_quantity_positive
    CHECK (quantity > 0)
    );

CREATE INDEX idx_movement_warehouse
    ON stock_movements(warehouse_id);

CREATE INDEX idx_movement_product
    ON stock_movements(product_id);

CREATE INDEX idx_movement_type
    ON stock_movements(movement_type);

CREATE INDEX idx_movement_created
    ON stock_movements(created_at);