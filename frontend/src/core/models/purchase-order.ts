export type PoStatus = 'DRAFT' | 'APPROVED' | 'PARTIALLY_RECEIVED' | 'RECEIVED' | 'CANCELLED';
export interface PurchaseOrderItem {
  id: number; productId: number; warehouseId: number;
  quantityOrdered: number; quantityReceived: number;
  unitPrice: number; createdAt: string;
}
export interface PurchaseOrder {
  id: number; supplierId: number; status: PoStatus;
  orderedBy: number; approvedBy: number; receivedBy: number;
  notes: string; expectedDate: string; receivedDate: string;
  createdAt: string; updatedAt: string; items: PurchaseOrderItem[];
}
export interface PurchaseOrderItemRequest {
  productId: number; warehouseId: number;
  quantityOrdered: number; unitPrice: number;
}
export interface PurchaseOrderRequest {
  supplierId: number; orderedBy: number;
  notes: string; expectedDate: string;
  items: PurchaseOrderItemRequest[];
}
export interface ApproveRequest { approvedBy: number; }
export interface ReceiveGoodsRequest { performedBy: number; }
