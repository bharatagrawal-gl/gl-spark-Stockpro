package com.stockpro.movement.controller;

import com.stockpro.movement.dto.*;
import com.stockpro.movement.entity.MovementType;
import com.stockpro.movement.service.MovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movements")
@RequiredArgsConstructor
public class MovementController {

    private final MovementService movementService;

    // Log a single STOCK_IN or STOCK_OUT
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<MovementResponse> logMovement(@RequestBody MovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movementService.logMovement(request));
    }

    // Log a TRANSFER — creates two records atomically
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<MovementResponse>> logTransfer(
            @RequestBody TransferMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movementService.logTransfer(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<MovementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(movementService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<MovementResponse>> getAll() {
        return ResponseEntity.ok(movementService.getAll());
    }

    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<List<MovementResponse>> getByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(movementService.getByWarehouse(warehouseId));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<List<MovementResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(movementService.getByProduct(productId));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<MovementResponse>> getByType(@PathVariable MovementType type) {
        return ResponseEntity.ok(movementService.getByType(type));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<MovementResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(movementService.getByPerformedBy(userId));
    }

    @GetMapping("/reference")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<MovementResponse>> getByReference(
            @RequestParam Long referenceId,
            @RequestParam String referenceType) {
        return ResponseEntity.ok(movementService.getByReference(referenceId, referenceType));
    }

    @GetMapping("/daterange")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<MovementResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(movementService.getByDateRange(from, to));
    }

    @GetMapping("/warehouse/{warehouseId}/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<List<MovementResponse>> getByWarehouseAndProduct(
            @PathVariable Long warehouseId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(movementService.getByWarehouseAndProduct(warehouseId, productId));
    }
}