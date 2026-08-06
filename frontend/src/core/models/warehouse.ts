export interface Warehouse {
  id: number;
  name: string;
  location: string;
  capacity: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
export interface WarehouseRequest { name: string; location: string; capacity: number; }
export interface StockLevel {
  id: number; warehouseId: number; productId: number;
  quantity: number; version: number; updatedAt: string;
}
export interface StockUpdateRequest { warehouseId: number; productId: number; quantity: number; }
export interface StockTransferRequest {
  fromWarehouseId: number; toWarehouseId: number;
  productId: number; quantity: number;
}
