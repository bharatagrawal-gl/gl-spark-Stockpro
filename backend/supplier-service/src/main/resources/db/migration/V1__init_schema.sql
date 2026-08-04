CREATE TABLE IF NOT EXISTS suppliers (
                                         id BIGSERIAL PRIMARY KEY,
                                         name VARCHAR(150) NOT NULL,
    contact_name VARCHAR(100),
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );