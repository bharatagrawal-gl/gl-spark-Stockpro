package com.stockpro.warehouse.service;

import com.stockpro.warehouse.dto.*;
import com.stockpro.warehouse.entity.*;
import com.stockpro.warehouse.event.StockEventPublisher;
import com.stockpro.warehouse.exception.*;
import com.stockpro.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockEventPublisher stockEventPublisher;
    private final RestTemplate restTemplate;  // ← add this

    @Value("${service.movement.url}")
    private String movementUrl;

    // ─── Warehouse CRUD ───────────────────────────────────────────────────────

    @Override
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Warehouse already exists with name: " + request.getName());
        }
        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .isActive(true)
                .build();
        return mapToWarehouseResponse(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseResponse getWarehouseById(Long id) {
        return mapToWarehouseResponse(findWarehouseOrThrow(id));
    }

    @Override
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll()
                .stream().map(this::mapToWarehouseResponse).collect(Collectors.toList());
    }

    @Override
    public List<WarehouseResponse> getActiveWarehouses() {
        return warehouseRepository.findByIsActive(true)
                .stream().map(this::mapToWarehouseResponse).collect(Collectors.toList());
    }

    @Override
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = findWarehouseOrThrow(id);
        if (!warehouse.getName().equals(request.getName())
                && warehouseRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Warehouse name already in use: " + request.getName());
        }
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setCapacity(request.getCapacity());
        return mapToWarehouseResponse(warehouseRepository.save(warehouse));
    }

    @Override
    public void activateWarehouse(Long id) {
        Warehouse warehouse = findWarehouseOrThrow(id);
        warehouse.setIsActive(true);
        warehouseRepository.save(warehouse);
    }

    @Override
    public void deactivateWarehouse(Long id) {
        Warehouse warehouse = findWarehouseOrThrow(id);
        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
    }

    // ─── Stock Operations ─────────────────────────────────────────────────────

    @Override
    public StockLevelResponse getStockLevel(Long warehouseId, Long productId) {
        StockLevel stock = stockLevelRepository
                .findByWarehouseIdAndProductId(warehouseId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No stock record found for warehouseId=" + warehouseId + ", productId=" + productId));
        return mapToStockResponse(stock);
    }

    @Override
    public List<StockLevelResponse> getStockByWarehouse(Long warehouseId) {
        findWarehouseOrThrow(warehouseId);
        return stockLevelRepository.findByWarehouseId(warehouseId)
                .stream().map(this::mapToStockResponse).collect(Collectors.toList());
    }

    @Override
    public List<StockLevelResponse> getStockByProduct(Long productId) {
        return stockLevelRepository.findByProductId(productId)
                .stream().map(this::mapToStockResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StockLevelResponse addStock(StockUpdateRequest request) {
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity to add must be positive.");
        }
        findWarehouseOrThrow(request.getWarehouseId());

        StockLevel stock = stockLevelRepository
                .findByWarehouseIdAndProductId(request.getWarehouseId(), request.getProductId())
                .orElseGet(() -> StockLevel.builder()
                        .warehouseId(request.getWarehouseId())
                        .productId(request.getProductId())
                        .quantity(0)
                        .build());

        stock.setQuantity(stock.getQuantity() + request.getQuantity());
        StockLevel saved = stockLevelRepository.save(stock);
        logMovement(saved.getWarehouseId(), saved.getProductId(),
                "STOCK_IN", request.getQuantity(),
                "Manual stock addition to warehouse " + saved.getWarehouseId());
        return mapToStockResponse(saved);
    }

    @Override
    @Transactional
    public StockLevelResponse deductStock(StockUpdateRequest request) {
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity to deduct must be positive.");
        }
        findWarehouseOrThrow(request.getWarehouseId());

        StockLevel stock = stockLevelRepository
                .findByWarehouseIdAndProductId(request.getWarehouseId(), request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No stock found for warehouseId=" + request.getWarehouseId()
                                + ", productId=" + request.getProductId()));

        if (stock.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + stock.getQuantity()
                            + ", Requested: " + request.getQuantity());
        }

        stock.setQuantity(stock.getQuantity() - request.getQuantity());

        StockLevel saved = stockLevelRepository.save(stock);
        stockEventPublisher.publishIfLowStock(
                saved.getWarehouseId(), saved.getProductId(), saved.getQuantity());

        logMovement(saved.getWarehouseId(), saved.getProductId(),
                "STOCK_OUT", request.getQuantity(),
                "Manual deduction from warehouse " + saved.getWarehouseId());
        return mapToStockResponse(saved);
    }

    @Override
    public List<StockLevelResponse> getAllStock() {
        return stockLevelRepository.findAll()
                .stream().map(this::mapToStockResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void transferStock(StockTransferRequest request) {
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Transfer quantity must be positive.");
        }
        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new RuntimeException("Source and destination warehouses must be different.");
        }

        findWarehouseOrThrow(request.getFromWarehouseId());
        findWarehouseOrThrow(request.getToWarehouseId());

        // Deduct from source — @Version will catch concurrent modification
        StockLevel source = stockLevelRepository
                .findByWarehouseIdAndProductId(request.getFromWarehouseId(), request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No stock found in source warehouse for this product."));

        if (source.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock in source warehouse. Available: " + source.getQuantity()
                            + ", Requested: " + request.getQuantity());
        }
        source.setQuantity(source.getQuantity() - request.getQuantity());


        // Add to destination — create record if it doesn't exist
        StockLevel destination = stockLevelRepository
                .findByWarehouseIdAndProductId(request.getToWarehouseId(), request.getProductId())
                .orElseGet(() -> StockLevel.builder()
                        .warehouseId(request.getToWarehouseId())
                        .productId(request.getProductId())
                        .quantity(0)
                        .build());

        destination.setQuantity(destination.getQuantity() + request.getQuantity());
        stockLevelRepository.save(source);
        stockLevelRepository.save(destination);
        stockEventPublisher.publishIfLowStock(
                source.getWarehouseId(), source.getProductId(), source.getQuantity());

        logMovement(request.getFromWarehouseId(), request.getProductId(),
                "STOCK_OUT", request.getQuantity(),
                "Transfer OUT to warehouse " + request.getToWarehouseId());

        logMovement(request.getToWarehouseId(), request.getProductId(),
                "STOCK_IN", request.getQuantity(),
                "Transfer IN from warehouse " + request.getFromWarehouseId());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Warehouse findWarehouseOrThrow(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    private WarehouseResponse mapToWarehouseResponse(Warehouse w) {
        return WarehouseResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .location(w.getLocation())
                .capacity(w.getCapacity())
                .isActive(w.getIsActive())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    private StockLevelResponse mapToStockResponse(StockLevel s) {
        return StockLevelResponse.builder()
                .id(s.getId())
                .warehouseId(s.getWarehouseId())
                .productId(s.getProductId())
                .quantity(s.getQuantity())
                .version(s.getVersion())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    // Add inside WarehouseServiceImpl — after all methods, before closing brace
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

    private void logMovement(Long warehouseId, Long productId,
                             String movementType, Integer quantity, String notes) {
        try {
            MovementLogRequest req = new MovementLogRequest(
                    warehouseId, productId, movementType, quantity,
                    null, "MANUAL", notes, 1L
            );
            restTemplate.postForObject(
                    movementUrl + "/api/v1/movements",
                    req,
                    Object.class
            );
        } catch (Exception e) {
            // Best-effort — never fail stock operation because of movement logging
            log.error("Movement log failed for warehouseId={}, productId={}: {}",
                    warehouseId, productId, e.getMessage());
        }
    }
}