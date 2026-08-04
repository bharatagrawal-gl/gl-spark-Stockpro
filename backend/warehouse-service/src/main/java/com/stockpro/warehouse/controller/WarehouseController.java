package com.stockpro.warehouse.controller;

import com.stockpro.warehouse.dto.*;
import com.stockpro.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    // ─── Warehouse Endpoints ──────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponse> createWarehouse(@RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.createWarehouse(request));
    }
    @GetMapping("/stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<List<StockLevelResponse>> getAllStock() {
        return ResponseEntity.ok(warehouseService.getAllStock());
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<List<WarehouseResponse>> getActiveWarehouses() {
        return ResponseEntity.ok(warehouseService.getActiveWarehouses());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponse> updateWarehouse(@PathVariable Long id,
                                                             @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, request));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateWarehouse(@PathVariable Long id) {
        warehouseService.activateWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateWarehouse(@PathVariable Long id) {
        warehouseService.deactivateWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Stock Endpoints ──────────────────────────────────────────────────────

    @GetMapping("/{warehouseId}/stock/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<StockLevelResponse> getStockLevel(@PathVariable Long warehouseId,
                                                            @PathVariable Long productId) {
        return ResponseEntity.ok(warehouseService.getStockLevel(warehouseId, productId));
    }

    @GetMapping("/{warehouseId}/stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<List<StockLevelResponse>> getStockByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(warehouseService.getStockByWarehouse(warehouseId));
    }

    @GetMapping("/stock/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<List<StockLevelResponse>> getStockByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(warehouseService.getStockByProduct(productId));
    }

    @PostMapping("/stock/add")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<StockLevelResponse> addStock(@RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(warehouseService.addStock(request));
    }

    @PostMapping("/stock/deduct")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<StockLevelResponse> deductStock(@RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(warehouseService.deductStock(request));
    }

    @PostMapping("/stock/transfer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> transferStock(@RequestBody StockTransferRequest request) {
        warehouseService.transferStock(request);
        return ResponseEntity.noContent().build();
    }
}