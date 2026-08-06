import { http } from '../http';
import { Product, ProductRequest } from '../models/product';

function normalizeProduct(product: Product & { active?: boolean }): Product {
  return { ...product, isActive: product.isActive ?? product.active ?? false };
}

export const productService = {
  async getAll(): Promise<Product[]> {
    const { data } = await http.get<Product[]>('/api/v1/products');
    return data.map(normalizeProduct);
  },
  async getById(id: number): Promise<Product> {
    const { data } = await http.get<Product>(`/api/v1/products/${id}`);
    return normalizeProduct(data);
  },
  async getActive(): Promise<Product[]> {
    const { data } = await http.get<Product[]>('/api/v1/products/active');
    return data.map(normalizeProduct);
  },
  async search(name: string): Promise<Product[]> {
    const { data } = await http.get<Product[]>(`/api/v1/products/search?name=${name}`);
    return data;
  },
  async create(request: ProductRequest): Promise<Product> {
    const { data } = await http.post<Product>('/api/v1/products', request);
    return normalizeProduct(data);
  },
  async update(id: number, request: ProductRequest): Promise<Product> {
    const { data } = await http.put<Product>(`/api/v1/products/${id}`, request);
    return normalizeProduct(data);
  },
  async activate(id: number): Promise<Product> {
    const { data } = await http.put<Product>(`/api/v1/products/${id}/activate`, {});
    return normalizeProduct(data);
  },
  async deactivate(id: number): Promise<Product> {
    const { data } = await http.put<Product>(`/api/v1/products/${id}/deactivate`, {});
    return normalizeProduct(data);
  },
  async delete(id: number): Promise<void> {
    await http.delete<void>(`/api/v1/products/${id}`);
  }
};
