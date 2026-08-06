package com.stockpro.report.service;

import com.stockpro.report.dto.LowStockResponse;
import com.stockpro.report.dto.StockValueResponse;

public interface ReportService {
    StockValueResponse getTotalStockValue(String authHeader);
    LowStockResponse getLowStockProducts(String authHeader);
}