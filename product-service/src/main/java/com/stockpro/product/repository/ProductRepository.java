package com.stockpro.product.repository;

import com.stockpro.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByIsActive(boolean isActive);
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
}