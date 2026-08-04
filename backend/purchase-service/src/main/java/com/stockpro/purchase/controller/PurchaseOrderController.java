package com.stockpro.purchase.controller;

import com.stockpro.purchase.dto.*;
import com.stockpro.purchase.entity.PoStatus;
import com.stockpro.purchase.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<PurchaseOrderResponse> create(
            @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseOrderService.createPurchaseOrder(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<PurchaseOrderResponse>> getAll() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<PurchaseOrderResponse>> getByStatus(
            @PathVariable PoStatus status) {
        return ResponseEntity.ok(purchaseOrderService.getByStatus(status));
    }

    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<PurchaseOrderResponse>> getBySupplier(
            @PathVariable Long supplierId) {
        return ResponseEntity.ok(purchaseOrderService.getBySupplier(supplierId));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PurchaseOrderResponse> approve(
            @PathVariable Long id,
            @RequestBody ApproveRequest request) {
        return ResponseEntity.ok(purchaseOrderService.approvePurchaseOrder(id, request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancelPurchaseOrder(id));
    }

    @PutMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<PurchaseOrderResponse> receiveGoods(
            @PathVariable Long id,
            @RequestBody ReceiveGoodsRequest request) {
        return ResponseEntity.ok(purchaseOrderService.receiveGoods(id, request));
    }
}