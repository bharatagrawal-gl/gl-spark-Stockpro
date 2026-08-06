export interface Product {
  id: number;
  name: string;
  sku: string;
  price: number;
  reorderLevel: number;
  barcode: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
export interface ProductRequest {
  name: string;
  sku: string;
  price: number;
  reorderLevel: number;
  barcode: string;
}
