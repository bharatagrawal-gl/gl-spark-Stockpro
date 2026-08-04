package com.stockpro.purchase.service;

import com.stockpro.purchase.dto.*;
import com.stockpro.purchase.entity.*;
import com.stockpro.purchase.event.PurchaseEventPublisher;
import com.stockpro.purchase.exception.*;
import com.stockpro.purchase.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderItemRepository itemRepository;
    private final RestTemplate restTemplate;
    private final PurchaseEventPublisher eventPublisher;
    @Value("${service.warehouse.url}")
    private String warehouseUrl;

    @Value("${service.movement.url}")
    private String movementUrl;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Purchase order must have at least one item.");
        }

        PurchaseOrder po = PurchaseOrder.builder()
                .supplierId(request.getSupplierId())
                .orderedBy(request.getOrderedBy())
                .notes(request.getNotes())
                .expectedDate(request.getExpectedDate())
                .status(PoStatus.DRAFT)
                .build();

        // Map items — link back to PO
        List<PurchaseOrderItem> items = request.getItems().stream()
                .map(itemReq -> PurchaseOrderItem.builder()
                        .purchaseOrder(po)
                        .productId(itemReq.getProductId())
                        .warehouseId(itemReq.getWarehouseId())
                        .quantityOrdered(itemReq.getQuantityOrdered())
                        .quantityReceived(0)
                        .unitPrice(itemReq.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        po.setItems(items);
        return mapToResponse(poRepository.save(po));
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        return mapToResponse(findPoOrThrow(id));
    }

    @Override
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return poRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderResponse> getByStatus(PoStatus status) {
        return poRepository.findByStatus(status)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderResponse> getBySupplier(Long supplierId) {
        return poRepository.findBySupplierId(supplierId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ─── Approve ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(Long id, ApproveRequest request) {
        PurchaseOrder po = findPoOrThrow(id);

        if (po.getStatus() != PoStatus.DRAFT) {
            throw new InvalidStatusTransitionException(
                    "Only DRAFT orders can be approved. Current status: " + po.getStatus());
        }

        po.setStatus(PoStatus.APPROVED);
        po.setApprovedBy(request.getApprovedBy());
        PurchaseOrder saved = poRepository.save(po);

        // Publish PO_OVERDUE event if expectedDate is already past
        if (saved.getExpectedDate() != null
                && saved.getExpectedDate().isBefore(java.time.LocalDate.now())) {
            eventPublisher.publishPoOverdue(
                    saved.getId(),
                    saved.getSupplierId(),
                    saved.getExpectedDate()
            );
        }

        return mapToResponse(saved);
    }

    // ─── Cancel ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long id) {
        PurchaseOrder po = findPoOrThrow(id);

        if (po.getStatus() == PoStatus.RECEIVED) {
            throw new InvalidStatusTransitionException(
                    "Cannot cancel a RECEIVED order.");
        }
        if (po.getStatus() == PoStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Order is already CANCELLED.");
        }

        po.setStatus(PoStatus.CANCELLED);
        return mapToResponse(poRepository.save(po));
    }

    // ─── Receive Goods ────────────────────────────────────────────────────────
    // ORCHESTRATION ORDER — STRICT:
    // 1. Validate PO is APPROVED
    // 2. Call warehouse-service to add stock for EACH item (FIRST)
    // 3. Update PO status to RECEIVED + set receivedDate (SECOND)
    // 4. Call movement-service to log STOCK_IN for each item (LAST — best effort)
    // DO NOT rollback steps 1-3 if step 4 fails

    @Override
    @Transactional
    public PurchaseOrderResponse receiveGoods(Long id, ReceiveGoodsRequest request) {
        PurchaseOrder po = findPoOrThrow(id);

        // Step 1: Validate status
        if (po.getStatus() != PoStatus.APPROVED) {
            throw new InvalidStatusTransitionException(
                    "Only APPROVED orders can be received. Current status: " + po.getStatus());
        }

        // Step 2: Call warehouse-service for each item
        for (PurchaseOrderItem item : po.getItems()) {
            StockUpdateRequest stockRequest = new StockUpdateRequest(
                    item.getWarehouseId(),
                    item.getProductId(),
                    item.getQuantityOrdered()
            );

            try {
                restTemplate.postForObject(
                        warehouseUrl + "/api/v1/warehouses/stock/add",
                        stockRequest,
                        Object.class
                );
                // Mark item as fully received
                item.setQuantityReceived(item.getQuantityOrdered());
            } catch (Exception e) {
                log.error("Failed to update warehouse stock for productId={}, warehouseId={}: {}",
                        item.getProductId(), item.getWarehouseId(), e.getMessage());
                throw new RuntimeException(
                        "Warehouse stock update failed for productId=" + item.getProductId()
                                + ". Receive goods aborted.");
            }
        }

        // Step 3: Update PO status — only after all warehouse calls succeed
        po.setStatus(PoStatus.RECEIVED);
        po.setReceivedDate(LocalDateTime.now());
        po.setReceivedBy(request.getPerformedBy());
        PurchaseOrder saved = poRepository.save(po);

        // Step 4: Log movements — best effort, do NOT throw if this fails
        for (PurchaseOrderItem item : saved.getItems()) {
            try {
                MovementLogRequest movementRequest = new MovementLogRequest(
                        item.getWarehouseId(),
                        item.getProductId(),
                        "STOCK_IN",
                        item.getQuantityOrdered(),
                        saved.getId(),
                        "PURCHASE_ORDER",
                        "Received from PO #" + saved.getId(),
                        request.getPerformedBy()
                );
                restTemplate.postForObject(
                        movementUrl + "/api/v1/movements",
                        movementRequest,
                        Object.class
                );
            } catch (Exception e) {
                // Best-effort — log and continue, do NOT throw
                log.error("Movement logging failed for productId={}, poId={}. " +
                                "Stock was already updated. Error: {}",
                        item.getProductId(), saved.getId(), e.getMessage());
            }
        }
        // Publish PO_OVERDUE if received after expected date
        if (po.getExpectedDate() != null
                && po.getExpectedDate().isBefore(java.time.LocalDate.now())) {
            try {
                eventPublisher.publishPoOverdue(
                        saved.getId(),
                        saved.getSupplierId(),
                        saved.getExpectedDate()
                );
            } catch (Exception e) {
                log.error("Failed to publish PO_OVERDUE event: {}", e.getMessage());
            }
        }

        return mapToResponse(saved);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private PurchaseOrder findPoOrThrow(Long id) {
        return poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase order not found with id: " + id));
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrder po) {
        List<PurchaseOrderItemResponse> itemResponses = po.getItems().stream()
                .map(item -> PurchaseOrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .warehouseId(item.getWarehouseId())
                        .quantityOrdered(item.getQuantityOrdered())
                        .quantityReceived(item.getQuantityReceived())
                        .unitPrice(item.getUnitPrice())
                        .createdAt(item.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .supplierId(po.getSupplierId())
                .status(po.getStatus())
                .orderedBy(po.getOrderedBy())
                .approvedBy(po.getApprovedBy())
                .receivedBy(po.getReceivedBy())
                .notes(po.getNotes())
                .expectedDate(po.getExpectedDate())
                .receivedDate(po.getReceivedDate())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .items(itemResponses)
                .build();
    }

    // ─── Inner DTOs for inter-service calls ───────────────────────────────────
    // These are local to ServiceImpl — not exposed via controller

    @lombok.Getter @lombok.Setter @lombok.AllArgsConstructor
    static class StockUpdateRequest {
        private Long warehouseId;
        private Long productId;
        private Integer quantity;
    }

    @lombok.Getter @lombok.Setter @lombok.AllArgsConstructor
    static class MovementLogRequest {
        private Long warehouseId;
        private Long productId;
        private String movementType;
        private Integer quantity;
        private Long referenceId;
        private String referenceType;
        private String notes;
        private Long performedBy;
    }
}