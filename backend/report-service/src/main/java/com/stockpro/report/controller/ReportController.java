package com.stockpro.report.controller;

import com.stockpro.report.dto.LowStockResponse;
import com.stockpro.report.dto.StockValueResponse;
import com.stockpro.report.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/stock-value")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AUDITOR')")
    public ResponseEntity<StockValueResponse> getStockValue(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return ResponseEntity.ok(reportService.getTotalStockValue(authHeader));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AUDITOR')")
    public ResponseEntity<LowStockResponse> getLowStock(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return ResponseEntity.ok(reportService.getLowStockProducts(authHeader));
    }
}