package com.stockpro.movement.service;

import com.stockpro.movement.dto.*;
import com.stockpro.movement.entity.*;
import com.stockpro.movement.exception.ResourceNotFoundException;
import com.stockpro.movement.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService {

    private final StockMovementRepository movementRepository;

    @Override
    @Transactional
    public MovementResponse logMovement(MovementRequest request) {
        if (request.getMovementType() == MovementType.TRANSFER) {
            throw new RuntimeException(
                    "Use POST /transfer to log transfer movements.");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be positive.");
        }

        StockMovement movement = StockMovement.builder()
                .warehouseId(request.getWarehouseId())
                .productId(request.getProductId())
                .movementType(request.getMovementType())
                .quantity(request.getQuantity())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .notes(request.getNotes())
                .performedBy(request.getPerformedBy())
                .build();

        return mapToResponse(movementRepository.save(movement));
    }

    @Override
    @Transactional
    public List<MovementResponse> logTransfer(TransferMovementRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Transfer quantity must be positive.");
        }
        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new RuntimeException("Source and destination warehouses must be different.");
        }

        // OUT record from source warehouse
        StockMovement outMovement = StockMovement.builder()
                .warehouseId(request.getFromWarehouseId())
                .productId(request.getProductId())
                .movementType(MovementType.TRANSFER)
                .quantity(request.getQuantity())
                .referenceId(request.getReferenceId())
                .referenceType("TRANSFER")
                .notes("Transfer OUT to warehouse " + request.getToWarehouseId()
                        + (request.getNotes() != null ? " — " + request.getNotes() : ""))
                .performedBy(request.getPerformedBy())
                .build();

        // IN record to destination warehouse
        StockMovement inMovement = StockMovement.builder()
                .warehouseId(request.getToWarehouseId())
                .productId(request.getProductId())
                .movementType(MovementType.TRANSFER)
                .quantity(request.getQuantity())
                .referenceId(request.getReferenceId())
                .referenceType("TRANSFER")
                .notes("Transfer IN from warehouse " + request.getFromWarehouseId()
                        + (request.getNotes() != null ? " — " + request.getNotes() : ""))
                .performedBy(request.getPerformedBy())
                .build();

        StockMovement savedOut = movementRepository.save(outMovement);
        StockMovement savedIn  = movementRepository.save(inMovement);

        return List.of(mapToResponse(savedOut), mapToResponse(savedIn));
    }

    @Override
    public MovementResponse getById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Override
    public List<MovementResponse> getAll() {
        return movementRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponse> getByWarehouse(Long warehouseId) {
        return movementRepository.findByWarehouseId(warehouseId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponse> getByProduct(Long productId) {
        return movementRepository.findByProductId(productId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponse> getByType(MovementType type) {
        return movementRepository.findByMovementType(type)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponse> getByPerformedBy(Long userId) {
        return movementRepository.findByPerformedBy(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponse> getByReference(Long referenceId, String referenceType) {
        return movementRepository.findByReferenceIdAndReferenceType(referenceId, referenceType)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponse> getByDateRange(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new RuntimeException("'from' date must be before 'to' date.");
        }
        return movementRepository.findByCreatedAtBetween(from, to)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponse> getByWarehouseAndProduct(Long warehouseId, Long productId) {
        return movementRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private StockMovement findOrThrow(Long id) {
        return movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movement record not found with id: " + id));
    }

    private MovementResponse mapToResponse(StockMovement m) {
        return MovementResponse.builder()
                .id(m.getId())
                .warehouseId(m.getWarehouseId())
                .productId(m.getProductId())
                .movementType(m.getMovementType())
                .quantity(m.getQuantity())
                .referenceId(m.getReferenceId())
                .referenceType(m.getReferenceType())
                .notes(m.getNotes())
                .performedBy(m.getPerformedBy())
                .createdAt(m.getCreatedAt())
                .build();
    }
}