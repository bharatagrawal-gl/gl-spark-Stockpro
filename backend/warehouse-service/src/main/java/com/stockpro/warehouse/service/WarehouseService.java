package com.stockpro.warehouse.service;

import com.stockpro.warehouse.dto.*;
import java.util.List;

public interface WarehouseService {

    // Warehouse CRUD
    WarehouseResponse createWarehouse(WarehouseRequest request);
    WarehouseResponse getWarehouseById(Long id);
    List<WarehouseResponse> getAllWarehouses();
    List<WarehouseResponse> getActiveWarehouses();
    WarehouseResponse updateWarehouse(Long id, WarehouseRequest request);
    void activateWarehouse(Long id);
    void deactivateWarehouse(Long id);

    // Stock operations
    StockLevelResponse getStockLevel(Long warehouseId, Long productId);
    List<StockLevelResponse> getStockByWarehouse(Long warehouseId);
    List<StockLevelResponse> getStockByProduct(Long productId);
    StockLevelResponse addStock(StockUpdateRequest request);
    StockLevelResponse deductStock(StockUpdateRequest request);
    void transferStock(StockTransferRequest request);

    public List<StockLevelResponse> getAllStock();
}