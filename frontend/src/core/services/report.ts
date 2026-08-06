import { http } from '../http';
import { LowStockResponse, StockValueResponse } from '../models/report';

export const reportService = {
  async getStockValue(): Promise<StockValueResponse> {
    const { data } = await http.get<StockValueResponse>('/api/v1/reports/stock-value');
    return data;
  },
  async getLowStock(): Promise<LowStockResponse> {
    const { data } = await http.get<LowStockResponse>('/api/v1/reports/low-stock');
    return data;
  }
};
