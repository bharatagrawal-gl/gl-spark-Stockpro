package com.stockpro.report.service;

import com.stockpro.report.dto.*;
import com.stockpro.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final RestTemplate restTemplate;

    @Value("${service.product.url}")
    private String productServiceUrl;

    @Value("${service.warehouse.url}")
    private String warehouseServiceUrl;

    @Override
    public StockValueResponse getTotalStockValue(String authHeader) {

        // Step 1 — get all products from product-service
        List<ProductDto> products = getAllProducts(authHeader);

        // Step 2 — get all stock levels from warehouse-service
        List<StockLevelDto> stockLevels = getAllStockLevels(authHeader);

        // Step 3 — build product price map for quick lookup
        Map<Long, ProductDto> productMap = products.stream()
                .collect(Collectors.toMap(ProductDto::getId, p -> p));

        // Step 4 — calculate value per product
        List<ProductStockValue> breakdown = new ArrayList<>();
        BigDecimal totalValue = BigDecimal.ZERO;
        int totalUnits = 0;

        for (StockLevelDto stock : stockLevels) {
            ProductDto product = productMap.get(stock.getProductId());
            if (product == null || product.getPrice() == null) continue;

            BigDecimal itemValue = product.getPrice()
                    .multiply(BigDecimal.valueOf(stock.getQuantity()));

            breakdown.add(new ProductStockValue(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    stock.getQuantity(),
                    product.getPrice(),
                    itemValue
            ));

            totalValue = totalValue.add(itemValue);
            totalUnits += stock.getQuantity();
        }

        return new StockValueResponse(
                totalValue,
                breakdown.size(),
                totalUnits,
                breakdown
        );
    }

    @Override
    public LowStockResponse getLowStockProducts(String authHeader) {

        // Step 1 — get all products
        List<ProductDto> products = getAllProducts(authHeader);

        // Step 2 — get all stock levels
        List<StockLevelDto> stockLevels = getAllStockLevels(authHeader);

        // Step 3 — build product map
        Map<Long, ProductDto> productMap = products.stream()
                .collect(Collectors.toMap(ProductDto::getId, p -> p));

        // Step 4 — find products below reorder level
        List<LowStockItem> lowStockItems = new ArrayList<>();

        for (StockLevelDto stock : stockLevels) {
            ProductDto product = productMap.get(stock.getProductId());
            if (product == null) continue;

            if (stock.getQuantity() < product.getReorderLevel()) {
                int shortage = product.getReorderLevel() - stock.getQuantity();
                lowStockItems.add(new LowStockItem(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        stock.getQuantity(),
                        product.getReorderLevel(),
                        shortage
                ));
            }
        }

        return new LowStockResponse(lowStockItems.size(), lowStockItems);
    }

    private List<ProductDto> getAllProducts(String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<ProductDto>> response = restTemplate.exchange(
                    productServiceUrl + "/api/v1/products",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<ProductDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to fetch products: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<StockLevelDto> getAllStockLevels(String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<StockLevelDto>> response = restTemplate.exchange(
                    warehouseServiceUrl + "/api/v1/warehouses/stock",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<StockLevelDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to fetch stock levels: {}", e.getMessage());
            return new ArrayList<>();
        }
    }}