-- V1__init_schema.sql

CREATE TABLE purchase_orders (
                                 id BIGSERIAL PRIMARY KEY,
                                 supplier_id BIGINT NOT NULL,

                                 status VARCHAR(25) NOT NULL DEFAULT 'DRAFT',

                                 ordered_by BIGINT NOT NULL,
                                 approved_by BIGINT,

                                 notes TEXT,
                                 expected_date DATE,
                                 received_date TIMESTAMP,

                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE purchase_order_items (
                                      id BIGSERIAL PRIMARY KEY,

                                      purchase_order_id BIGINT NOT NULL,
                                      product_id BIGINT NOT NULL,
                                      warehouse_id BIGINT NOT NULL,

                                      quantity_ordered INT NOT NULL,
                                      quantity_received INT NOT NULL DEFAULT 0,

                                      unit_price DECIMAL(12,2) NOT NULL,

                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_item_po
                                          FOREIGN KEY (purchase_order_id)
                                              REFERENCES purchase_orders(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT chk_qty_ordered
                                          CHECK (quantity_ordered > 0),

                                      CONSTRAINT chk_unit_price
                                          CHECK (unit_price > 0)
);

-- Indexes
CREATE INDEX idx_po_supplier
    ON purchase_orders(supplier_id);

CREATE INDEX idx_po_status
    ON purchase_orders(status);

CREATE INDEX idx_po_ordered
    ON purchase_orders(ordered_by);

CREATE INDEX idx_item_po
    ON purchase_order_items(purchase_order_id);

CREATE INDEX idx_item_product
    ON purchase_order_items(product_id);