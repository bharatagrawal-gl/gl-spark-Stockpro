package com.stockpro.report;

import com.stockpro.report.dto.*;
import com.stockpro.report.service.ReportServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImpl Unit Tests")
class ReportServiceImplTest {

    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private ReportServiceImpl reportService;

    private static final String AUTH = "Bearer test-token";

    // Inject @Value fields manually since Mockito doesn't handle them
    @BeforeEach
    void setUp() throws Exception {
        var productField = ReportServiceImpl.class.getDeclaredField("productServiceUrl");
        productField.setAccessible(true);
        productField.set(reportService, "http://localhost:8082");

        var warehouseField = ReportServiceImpl.class.getDeclaredField("warehouseServiceUrl");
        warehouseField.setAccessible(true);
        warehouseField.set(reportService, "http://localhost:8083");
    }

    // ─── Test Data Helpers ────────────────────────────────────────────────────

    private ProductDto product(Long id, String name, String sku,
                               BigDecimal price, int reorderLevel) {
        ProductDto p = new ProductDto();
        p.setId(id);
        p.setName(name);
        p.setSku(sku);
        p.setPrice(price);
        p.setReorderLevel(reorderLevel);
        p.setActive(true);
        return p;
    }

    private StockLevelDto stock(Long productId, Long warehouseId, int qty) {
        StockLevelDto s = new StockLevelDto();
        s.setProductId(productId);
        s.setWarehouseId(warehouseId);
        s.setQuantity(qty);
        return s;
    }

    // Stub restTemplate.exchange() for products endpoint
    private void mockProducts(List<ProductDto> products) {
        when(restTemplate.exchange(
                contains("/api/v1/products"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(products));
    }

    // Stub restTemplate.exchange() for stock endpoint
    private void mockStock(List<StockLevelDto> stocks) {
        when(restTemplate.exchange(
                contains("/api/v1/warehouses/stock"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(stocks));
    }

    // ─── getTotalStockValue() ─────────────────────────────────────────────────

    @Test
    @DisplayName("getTotalStockValue() - correctly calculates total value from products and stock")
    void getTotalStockValue_correctCalculation() {
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 5),
                product(2L, "Mouse", "MOU-001", new BigDecimal("800.00"), 10)
        ));
        mockStock(List.of(
                stock(1L, 1L, 10),   // 10 × 45000 = 450000
                stock(2L, 1L, 50)    // 50 × 800   =  40000
        ));

        StockValueResponse res = reportService.getTotalStockValue(AUTH);

        assertThat(res.getTotalStockValue())
                .isEqualByComparingTo(new BigDecimal("490000.00"));
        assertThat(res.getTotalProducts()).isEqualTo(2);
        assertThat(res.getTotalUnits()).isEqualTo(60);  // 10 + 50
        assertThat(res.getBreakdown()).hasSize(2);
    }

    @Test
    @DisplayName("getTotalStockValue() - returns zero when no stock exists")
    void getTotalStockValue_returnsZero_whenNoStock() {
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 5)
        ));
        mockStock(List.of()); // no stock levels

        StockValueResponse res = reportService.getTotalStockValue(AUTH);

        assertThat(res.getTotalStockValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(res.getTotalProducts()).isEqualTo(0);
        assertThat(res.getTotalUnits()).isEqualTo(0);
        assertThat(res.getBreakdown()).isEmpty();
    }

    @Test
    @DisplayName("getTotalStockValue() - returns zero when no products exist")
    void getTotalStockValue_returnsZero_whenNoProducts() {
        mockProducts(List.of()); // no products
        mockStock(List.of(
                stock(1L, 1L, 10)
        ));

        StockValueResponse res = reportService.getTotalStockValue(AUTH);

        // stock exists but product not found in map → skipped
        assertThat(res.getTotalStockValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(res.getBreakdown()).isEmpty();
    }

    @Test
    @DisplayName("getTotalStockValue() - skips stock entries where product is not found")
    void getTotalStockValue_skipsOrphanStockEntries() {
        // Only product 1 exists, but stock has product 1 and 999
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 5)
        ));
        mockStock(List.of(
                stock(1L, 1L, 10),    // valid
                stock(999L, 1L, 5)    // orphan — no product 999
        ));

        StockValueResponse res = reportService.getTotalStockValue(AUTH);

        // Only product 1 counted: 10 × 45000 = 450000
        assertThat(res.getTotalProducts()).isEqualTo(1);
        assertThat(res.getTotalUnits()).isEqualTo(10);
        assertThat(res.getTotalStockValue())
                .isEqualByComparingTo(new BigDecimal("450000.00"));
    }

    @Test
    @DisplayName("getTotalStockValue() - breakdown contains correct per-product values")
    void getTotalStockValue_breakdownIsCorrect() {
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 5)
        ));
        mockStock(List.of(
                stock(1L, 1L, 4)   // 4 × 45000 = 180000
        ));

        StockValueResponse res = reportService.getTotalStockValue(AUTH);

        assertThat(res.getBreakdown()).hasSize(1);
        ProductStockValue item = res.getBreakdown().get(0);
        assertThat(item.getProductName()).isEqualTo("Dell Laptop");
        assertThat(item.getSku()).isEqualTo("LAP-001");
        assertThat(item.getQuantity()).isEqualTo(4);
        assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal("45000.00"));
        assertThat(item.getTotalValue()).isEqualByComparingTo(new BigDecimal("180000.00"));
    }

    @Test
    @DisplayName("getTotalStockValue() - returns empty response when product service fails")
    void getTotalStockValue_returnsEmpty_whenProductServiceThrows() {
        when(restTemplate.exchange(
                contains("/api/v1/products"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("product-service down"));

        // stock call still happens but product map is empty
        mockStock(List.of(stock(1L, 1L, 10)));

        StockValueResponse res = reportService.getTotalStockValue(AUTH);

        // graceful degradation — no crash, just empty result
        assertThat(res.getTotalStockValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(res.getBreakdown()).isEmpty();
    }

    @Test
    @DisplayName("getTotalStockValue() - returns empty response when warehouse service fails")
    void getTotalStockValue_returnsEmpty_whenWarehouseServiceThrows() {
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 5)
        ));
        when(restTemplate.exchange(
                contains("/api/v1/warehouses/stock"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("warehouse-service down"));

        StockValueResponse res = reportService.getTotalStockValue(AUTH);

        assertThat(res.getTotalStockValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(res.getBreakdown()).isEmpty();
    }

    // ─── getLowStockProducts() ────────────────────────────────────────────────

    @Test
    @DisplayName("getLowStockProducts() - returns products below reorder level")
    void getLowStockProducts_returnsBelowReorderLevel() {
        // product 1: reorderLevel=5, stock=3 → LOW (shortage=2)
        // product 2: reorderLevel=10, stock=15 → OK
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 5),
                product(2L, "Mouse", "MOU-001", new BigDecimal("800.00"), 10)
        ));
        mockStock(List.of(
                stock(1L, 1L, 3),
                stock(2L, 1L, 15)
        ));

        LowStockResponse res = reportService.getLowStockProducts(AUTH);

        assertThat(res.getTotalLowStockProducts()).isEqualTo(1);
        assertThat(res.getItems()).hasSize(1);

        LowStockItem item = res.getItems().get(0);
        assertThat(item.getProductName()).isEqualTo("Dell Laptop");
        assertThat(item.getCurrentQuantity()).isEqualTo(3);
        assertThat(item.getReorderLevel()).isEqualTo(5);
        assertThat(item.getShortage()).isEqualTo(2); // 5 - 3 = 2
    }

    @Test
    @DisplayName("getLowStockProducts() - returns empty when all products adequately stocked")
    void getLowStockProducts_returnsEmpty_whenAllAdequate() {
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 5),
                product(2L, "Mouse", "MOU-001", new BigDecimal("800.00"), 10)
        ));
        mockStock(List.of(
                stock(1L, 1L, 100),
                stock(2L, 1L, 100)
        ));

        LowStockResponse res = reportService.getLowStockProducts(AUTH);

        assertThat(res.getTotalLowStockProducts()).isEqualTo(0);
        assertThat(res.getItems()).isEmpty();
    }

    @Test
    @DisplayName("getLowStockProducts() - returns empty when no products exist")
    void getLowStockProducts_returnsEmpty_whenNoProducts() {
        mockProducts(List.of());
        mockStock(List.of(stock(1L, 1L, 3)));

        LowStockResponse res = reportService.getLowStockProducts(AUTH);

        assertThat(res.getTotalLowStockProducts()).isEqualTo(0);
        assertThat(res.getItems()).isEmpty();
    }

    @Test
    @DisplayName("getLowStockProducts() - correctly calculates shortage amount")
    void getLowStockProducts_calculatesShortageCorrectly() {
        // reorderLevel=20, qty=7 → shortage = 13
        mockProducts(List.of(
                product(1L, "Keyboard", "KEY-001", new BigDecimal("1200.00"), 20)
        ));
        mockStock(List.of(stock(1L, 1L, 7)));

        LowStockResponse res = reportService.getLowStockProducts(AUTH);

        assertThat(res.getItems().get(0).getShortage()).isEqualTo(13);
    }

    @Test
    @DisplayName("getLowStockProducts() - stock exactly at reorder level is NOT flagged")
    void getLowStockProducts_notFlagged_whenStockExactlyAtReorderLevel() {
        // qty == reorderLevel → not low stock (condition is qty < reorderLevel)
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 10)
        ));
        mockStock(List.of(stock(1L, 1L, 10))); // exactly at threshold

        LowStockResponse res = reportService.getLowStockProducts(AUTH);

        assertThat(res.getTotalLowStockProducts()).isEqualTo(0);
        assertThat(res.getItems()).isEmpty();
    }

    @Test
    @DisplayName("getLowStockProducts() - all products low when all below reorder level")
    void getLowStockProducts_allLow_whenAllBelowReorderLevel() {
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 10),
                product(2L, "Mouse", "MOU-001", new BigDecimal("800.00"), 20)
        ));
        mockStock(List.of(
                stock(1L, 1L, 2),   // 2 < 10 → low
                stock(2L, 1L, 5)    // 5 < 20 → low
        ));

        LowStockResponse res = reportService.getLowStockProducts(AUTH);

        assertThat(res.getTotalLowStockProducts()).isEqualTo(2);
        assertThat(res.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("getLowStockProducts() - returns empty gracefully when product service fails")
    void getLowStockProducts_returnsEmpty_whenProductServiceFails() {
        when(restTemplate.exchange(
                contains("/api/v1/products"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("service down"));

        mockStock(List.of(stock(1L, 1L, 2)));

        LowStockResponse res = reportService.getLowStockProducts(AUTH);

        // No crash — graceful empty result
        assertThat(res.getTotalLowStockProducts()).isEqualTo(0);
        assertThat(res.getItems()).isEmpty();
    }

    @Test
    @DisplayName("getLowStockProducts() - skips stock entries with no matching product")
    void getLowStockProducts_skipsOrphanStock() {
        mockProducts(List.of(
                product(1L, "Dell Laptop", "LAP-001", new BigDecimal("45000.00"), 5)
        ));
        mockStock(List.of(
                stock(1L, 1L, 2),     // valid → low
                stock(999L, 1L, 1)    // orphan → skipped
        ));

        LowStockResponse res = reportService.getLowStockProducts(AUTH);

        assertThat(res.getTotalLowStockProducts()).isEqualTo(1);
        assertThat(res.getItems().get(0).getProductId()).isEqualTo(1L);
    }
}