import { http } from '../http';
import { ApproveRequest, PurchaseOrder, PurchaseOrderRequest, ReceiveGoodsRequest } from '../models/purchase-order';

export const purchaseOrderService = {
  async getAll(): Promise<PurchaseOrder[]> {
    const { data } = await http.get<PurchaseOrder[]>('/api/v1/purchase-orders');
    return data;
  },
  async getById(id: number): Promise<PurchaseOrder> {
    const { data } = await http.get<PurchaseOrder>(`/api/v1/purchase-orders/${id}`);
    return data;
  },
  async create(request: PurchaseOrderRequest): Promise<PurchaseOrder> {
    const { data } = await http.post<PurchaseOrder>('/api/v1/purchase-orders', request);
    return data;
  },
  async approve(id: number, request: ApproveRequest): Promise<PurchaseOrder> {
    const { data } = await http.put<PurchaseOrder>(`/api/v1/purchase-orders/${id}/approve`, request);
    return data;
  },
  async cancel(id: number): Promise<PurchaseOrder> {
    const { data } = await http.put<PurchaseOrder>(`/api/v1/purchase-orders/${id}/cancel`, {});
    return data;
  },
  async receiveGoods(id: number, request: ReceiveGoodsRequest): Promise<PurchaseOrder> {
    const { data } = await http.put<PurchaseOrder>(`/api/v1/purchase-orders/${id}/receive`, request);
    return data;
  }
};
