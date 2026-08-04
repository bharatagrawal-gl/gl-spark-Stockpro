-- src/main/resources/db/migration/V1__init_schema.sql

CREATE TABLE warehouses (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            location VARCHAR(255) NOT NULL,
                            capacity INT NOT NULL,
                            is_active BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT uq_warehouse_name UNIQUE (name)
);

CREATE TABLE stock_levels (
                              id BIGSERIAL PRIMARY KEY,
                              warehouse_id BIGINT NOT NULL,
                              product_id BIGINT NOT NULL,
                              quantity INT NOT NULL DEFAULT 0,
                              version BIGINT NOT NULL DEFAULT 0,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT uq_warehouse_product UNIQUE (warehouse_id, product_id),

                              CONSTRAINT chk_quantity_non_negative
                                  CHECK (quantity >= 0),

                              CONSTRAINT fk_stock_warehouse
                                  FOREIGN KEY (warehouse_id)
                                      REFERENCES warehouses(id)
                                      ON DELETE CASCADE
);