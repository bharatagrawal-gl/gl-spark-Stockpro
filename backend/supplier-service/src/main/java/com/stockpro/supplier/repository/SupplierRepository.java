package com.stockpro.supplier.repository;

import com.stockpro.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByIsActive(boolean isActive);
    Optional<Supplier> findByEmail(String email);
    boolean existsByEmail(String email);
}