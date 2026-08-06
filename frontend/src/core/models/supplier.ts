export interface Supplier {
  id: number;
  name: string;
  contactName: string;
  email: string;
  phone: string;
  address: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
export interface SupplierRequest {
  name: string;
  contactName: string;
  email: string;
  phone: string;
  address: string;
}
