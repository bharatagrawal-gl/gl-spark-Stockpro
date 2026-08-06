import { http } from '../http';
import { MovementRequest, MovementType, StockMovement } from '../models/movement';

export const movementService = {
  async getAll(): Promise<StockMovement[]> {
    const { data } = await http.get<StockMovement[]>('/api/v1/movements');
    return data;
  },
  async getByWarehouse(warehouseId: number): Promise<StockMovement[]> {
    const { data } = await http.get<StockMovement[]>(`/api/v1/movements/warehouse/${warehouseId}`);
    return data;
  },
  async getByProduct(productId: number): Promise<StockMovement[]> {
    const { data } = await http.get<StockMovement[]>(`/api/v1/movements/product/${productId}`);
    return data;
  },
  async getByType(type: MovementType): Promise<StockMovement[]> {
    const { data } = await http.get<StockMovement[]>(`/api/v1/movements/type/${type}`);
    return data;
  },
  async create(request: MovementRequest): Promise<StockMovement> {
    const { data } = await http.post<StockMovement>('/api/v1/movements', request);
    return data;
  }
};
