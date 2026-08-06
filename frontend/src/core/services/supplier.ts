import { http } from '../http';
import { Supplier, SupplierRequest } from '../models/supplier';

function normalizeSupplier(supplier: Supplier & { active?: boolean }): Supplier {
  return { ...supplier, isActive: supplier.isActive ?? supplier.active ?? false };
}

export const supplierService = {
  async getAll(): Promise<Supplier[]> {
    const { data } = await http.get<Supplier[]>('/api/v1/suppliers');
    return data.map(normalizeSupplier);
  },
  async getActive(): Promise<Supplier[]> {
    const { data } = await http.get<Supplier[]>('/api/v1/suppliers/active');
    return data.map(normalizeSupplier);
  },
  async create(request: SupplierRequest): Promise<Supplier> {
    const { data } = await http.post<Supplier>('/api/v1/suppliers', request);
    return normalizeSupplier(data);
  },
  async update(id: number, request: SupplierRequest): Promise<Supplier> {
    const { data } = await http.put<Supplier>(`/api/v1/suppliers/${id}`, request);
    return normalizeSupplier(data);
  },
  async activate(id: number): Promise<Supplier> {
    const { data } = await http.put<Supplier>(`/api/v1/suppliers/${id}/activate`, {});
    return normalizeSupplier(data);
  },
  async deactivate(id: number): Promise<Supplier> {
    const { data } = await http.put<Supplier>(`/api/v1/suppliers/${id}/deactivate`, {});
    return normalizeSupplier(data);
  },
  async delete(id: number): Promise<void> {
    await http.delete<void>(`/api/v1/suppliers/${id}`);
  }
};
