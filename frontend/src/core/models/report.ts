export interface ProductStockValue {
  productId: number; productName: string; sku: string;
  quantity: number; unitPrice: number; totalValue: number;
}
export interface StockValueResponse {
  totalStockValue: number; totalProducts: number;
  totalUnits: number; breakdown: ProductStockValue[];
}
export interface LowStockItem {
  productId: number; productName: string; sku: string;
  currentQuantity: number; reorderLevel: number; shortage: number;
}
export interface LowStockResponse {
  totalLowStockProducts: number; items: LowStockItem[];
}
