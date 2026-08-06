export type MovementType = 'STOCK_IN' | 'STOCK_OUT' | 'TRANSFER';
export interface StockMovement {
  id: number; warehouseId: number; productId: number;
  movementType: MovementType; quantity: number;
  referenceId: number; referenceType: string;
  notes: string; performedBy: number; createdAt: string;
}
export interface MovementRequest {
  warehouseId: number; productId: number;
  movementType: MovementType; quantity: number;
  referenceId?: number; referenceType?: string;
  notes?: string; performedBy: number;
}
