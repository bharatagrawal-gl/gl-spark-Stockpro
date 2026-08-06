import { http } from '../http';
import { StockLevel, StockTransferRequest, StockUpdateRequest, Warehouse, WarehouseRequest } from '../models/warehouse';

function normalizeWarehouse(warehouse: Warehouse & { active?: boolean }): Warehouse {
  return { ...warehouse, isActive: warehouse.isActive ?? warehouse.active ?? false };
}

export const warehouseService = {
  async getAll(): Promise<Warehouse[]> {
    const { data } = await http.get<Warehouse[]>('/api/v1/warehouses');
    return data.map(normalizeWarehouse);
  },
  async getActive(): Promise<Warehouse[]> {
    const { data } = await http.get<Warehouse[]>('/api/v1/warehouses/active');
    return data.map(normalizeWarehouse);
  },
  async create(request: WarehouseRequest): Promise<Warehouse> {
    const { data } = await http.post<Warehouse>('/api/v1/warehouses', request);
    return normalizeWarehouse(data);
  },
  async update(id: number, request: WarehouseRequest): Promise<Warehouse> {
    const { data } = await http.put<Warehouse>(`/api/v1/warehouses/${id}`, request);
    return normalizeWarehouse(data);
  },
  async activate(id: number): Promise<Warehouse> {
    const { data } = await http.put<Warehouse>(`/api/v1/warehouses/${id}/activate`, {});
    return normalizeWarehouse(data);
  },
  async deactivate(id: number): Promise<Warehouse> {
    const { data } = await http.put<Warehouse>(`/api/v1/warehouses/${id}/deactivate`, {});
    return normalizeWarehouse(data);
  },
  async getStockByWarehouse(warehouseId: number): Promise<StockLevel[]> {
    const { data } = await http.get<StockLevel[]>(`/api/v1/warehouses/${warehouseId}/stock`);
    return data;
  },
  async getAllStock(): Promise<StockLevel[]> {
    const { data } = await http.get<StockLevel[]>('/api/v1/warehouses/stock');
    return data;
  },
  async addStock(request: StockUpdateRequest): Promise<StockLevel> {
    const { data } = await http.post<StockLevel>('/api/v1/warehouses/stock/add', request);
    return data;
  },
  async deductStock(request: StockUpdateRequest): Promise<StockLevel> {
    const { data } = await http.post<StockLevel>('/api/v1/warehouses/stock/deduct', request);
    return data;
  },
  async transferStock(request: StockTransferRequest): Promise<void> {
    await http.post<void>('/api/v1/warehouses/stock/transfer', request);
  }
};
