package com.stockpro.warehouse;

import com.stockpro.warehouse.dto.*;
import com.stockpro.warehouse.entity.*;
import com.stockpro.warehouse.event.StockEventPublisher;
import com.stockpro.warehouse.exception.*;
import com.stockpro.warehouse.repository.*;
import com.stockpro.warehouse.service.WarehouseServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseServiceImpl Unit Tests")
class WarehouseServiceImplTest {

    @Mock private WarehouseRepository warehouseRepository;
    @Mock private StockLevelRepository stockLevelRepository;
    @Mock private StockEventPublisher stockEventPublisher;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    // We need to inject @Value field manually since Mockito doesn't handle it
    @BeforeEach
    void setUp() throws Exception {
        var field = WarehouseServiceImpl.class.getDeclaredField("movementUrl");
        field.setAccessible(true);
        field.set(warehouseService, "http://localhost:8086");
    }

    // ─── Test Data Helpers ────────────────────────────────────────────────────

    private Warehouse buildWarehouse(Long id, String name, boolean active) {
        Warehouse w = new Warehouse();
        w.setId(id);
        w.setName(name);
        w.setLocation("Mumbai");
        w.setCapacity(10000);
        w.setIsActive(active);
        w.setCreatedAt(LocalDateTime.now());
        w.setUpdatedAt(LocalDateTime.now());
        return w;
    }

    private StockLevel buildStock(Long id, Long warehouseId, Long productId, int qty) {
        StockLevel s = new StockLevel();
        s.setId(id);
        s.setWarehouseId(warehouseId);
        s.setProductId(productId);
        s.setQuantity(qty);
        s.setVersion(0L);
        s.setUpdatedAt(LocalDateTime.now());
        return s;
    }

    // ─── createWarehouse() ────────────────────────────────────────────────────

    @Test
    @DisplayName("createWarehouse() - success when name is unique")
    void createWarehouse_success() {
        WarehouseRequest req = new WarehouseRequest("Mumbai WH", "Mumbai", 10000);
        Warehouse saved = buildWarehouse(1L, "Mumbai WH", true);

        when(warehouseRepository.existsByName("Mumbai WH")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);

        WarehouseResponse res = warehouseService.createWarehouse(req);

        assertThat(res.getName()).isEqualTo("Mumbai WH");
        assertThat(res.getIsActive()).isTrue();
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("createWarehouse() - throws DuplicateResourceException when name exists")
    void createWarehouse_throwsDuplicate_whenNameExists() {
        when(warehouseRepository.existsByName("Mumbai WH")).thenReturn(true);

        assertThatThrownBy(() ->
                warehouseService.createWarehouse(new WarehouseRequest("Mumbai WH", "Mumbai", 5000)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mumbai WH");

        verify(warehouseRepository, never()).save(any());
    }

    // ─── getWarehouseById() ───────────────────────────────────────────────────

    @Test
    @DisplayName("getWarehouseById() - returns warehouse when found")
    void getWarehouseById_returnsWarehouse() {
        Warehouse w = buildWarehouse(1L, "Delhi WH", true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));

        WarehouseResponse res = warehouseService.getWarehouseById(1L);

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getName()).isEqualTo("Delhi WH");
    }

    @Test
    @DisplayName("getWarehouseById() - throws ResourceNotFoundException when not found")
    void getWarehouseById_throwsNotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getWarehouseById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── getAllWarehouses() ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAllWarehouses() - returns all warehouses")
    void getAllWarehouses_returnsAll() {
        when(warehouseRepository.findAll())
                .thenReturn(List.of(
                        buildWarehouse(1L, "WH1", true),
                        buildWarehouse(2L, "WH2", false)
                ));

        List<WarehouseResponse> result = warehouseService.getAllWarehouses();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getAllWarehouses() - returns empty list when none exist")
    void getAllWarehouses_returnsEmpty() {
        when(warehouseRepository.findAll()).thenReturn(List.of());

        assertThat(warehouseService.getAllWarehouses()).isEmpty();
    }

    // ─── getActiveWarehouses() ────────────────────────────────────────────────

    @Test
    @DisplayName("getActiveWarehouses() - returns only active warehouses")
    void getActiveWarehouses_returnsOnlyActive() {
        when(warehouseRepository.findByIsActive(true))
                .thenReturn(List.of(buildWarehouse(1L, "Active WH", true)));

        List<WarehouseResponse> result = warehouseService.getActiveWarehouses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(warehouseRepository).findByIsActive(true);
    }

    // ─── updateWarehouse() ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateWarehouse() - success when name unchanged")
    void updateWarehouse_success_sameName() {
        Warehouse w = buildWarehouse(1L, "Mumbai WH", true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(warehouseRepository.save(any())).thenReturn(w);

        WarehouseResponse res = warehouseService.updateWarehouse(1L,
                new WarehouseRequest("Mumbai WH", "Pune", 8000));

        assertThat(res).isNotNull();
        verify(warehouseRepository, never()).existsByName(any());
    }

    @Test
    @DisplayName("updateWarehouse() - throws DuplicateResourceException when new name taken")
    void updateWarehouse_throwsDuplicate_whenNewNameTaken() {
        Warehouse w = buildWarehouse(1L, "Mumbai WH", true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(warehouseRepository.existsByName("Delhi WH")).thenReturn(true);

        assertThatThrownBy(() ->
                warehouseService.updateWarehouse(1L,
                        new WarehouseRequest("Delhi WH", "Delhi", 5000)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Delhi WH");

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateWarehouse() - throws ResourceNotFoundException when warehouse missing")
    void updateWarehouse_throwsNotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                warehouseService.updateWarehouse(99L,
                        new WarehouseRequest("X", "Y", 100)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── activateWarehouse() / deactivateWarehouse() ──────────────────────────

    @Test
    @DisplayName("deactivateWarehouse() - sets isActive to false")
    void deactivateWarehouse_setsActiveFalse() {
        Warehouse w = buildWarehouse(1L, "WH1", true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));

        warehouseService.deactivateWarehouse(1L);

        assertThat(w.getIsActive()).isFalse();
        verify(warehouseRepository).save(w);
    }

    @Test
    @DisplayName("activateWarehouse() - sets isActive to true")
    void activateWarehouse_setsActiveTrue() {
        Warehouse w = buildWarehouse(1L, "WH1", false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));

        warehouseService.activateWarehouse(1L);

        assertThat(w.getIsActive()).isTrue();
        verify(warehouseRepository).save(w);
    }

    // ─── getStockLevel() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getStockLevel() - returns stock when found")
    void getStockLevel_returnsStock() {
        StockLevel s = buildStock(1L, 1L, 10L, 50);
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(s));

        StockLevelResponse res = warehouseService.getStockLevel(1L, 10L);

        assertThat(res.getQuantity()).isEqualTo(50);
        assertThat(res.getProductId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getStockLevel() - throws ResourceNotFoundException when not found")
    void getStockLevel_throwsNotFound() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getStockLevel(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── addStock() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("addStock() - creates new stock record when none exists")
    void addStock_createsNewRecord() {
        Warehouse w = buildWarehouse(1L, "WH1", true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.empty());

        StockLevel saved = buildStock(1L, 1L, 10L, 20);
        when(stockLevelRepository.save(any())).thenReturn(saved);

        StockUpdateRequest req = new StockUpdateRequest(1L, 10L, 20);
        StockLevelResponse res = warehouseService.addStock(req);

        assertThat(res.getQuantity()).isEqualTo(20);
        verify(stockLevelRepository).save(any());
    }

    @Test
    @DisplayName("addStock() - adds to existing stock record")
    void addStock_addsToExisting() {
        Warehouse w = buildWarehouse(1L, "WH1", true);
        StockLevel existing = buildStock(1L, 1L, 10L, 30);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(existing));
        when(stockLevelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StockUpdateRequest req = new StockUpdateRequest(1L, 10L, 20);
        StockLevelResponse res = warehouseService.addStock(req);

        // 30 + 20 = 50
        assertThat(res.getQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("addStock() - throws RuntimeException when quantity <= 0")
    void addStock_throwsException_whenQuantityZero() {
        StockUpdateRequest req = new StockUpdateRequest(1L, 10L, 0);

        assertThatThrownBy(() -> warehouseService.addStock(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("addStock() - throws ResourceNotFoundException when warehouse not found")
    void addStock_throwsNotFound_whenWarehouseMissing() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                warehouseService.addStock(new StockUpdateRequest(99L, 10L, 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deductStock() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deductStock() - success when sufficient stock available")
    void deductStock_success() {
        Warehouse w = buildWarehouse(1L, "WH1", true);
        StockLevel stock = buildStock(1L, 1L, 10L, 50);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(stock));
        when(stockLevelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(stockEventPublisher).publishIfLowStock(any(), any(), any());

        StockUpdateRequest req = new StockUpdateRequest(1L, 10L, 20);
        StockLevelResponse res = warehouseService.deductStock(req);

        // 50 - 20 = 30
        assertThat(res.getQuantity()).isEqualTo(30);
        verify(stockEventPublisher).publishIfLowStock(1L, 10L, 30);
    }

    @Test
    @DisplayName("deductStock() - throws InsufficientStockException when stock too low")
    void deductStock_throwsInsufficientStock() {
        Warehouse w = buildWarehouse(1L, "WH1", true);
        StockLevel stock = buildStock(1L, 1L, 10L, 5);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(stock));

        assertThatThrownBy(() ->
                warehouseService.deductStock(new StockUpdateRequest(1L, 10L, 100)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Available: 5");

        verify(stockLevelRepository, never()).save(any());
    }

    @Test
    @DisplayName("deductStock() - throws RuntimeException when quantity is zero or negative")
    void deductStock_throwsException_whenQuantityZero() {
        assertThatThrownBy(() ->
                warehouseService.deductStock(new StockUpdateRequest(1L, 10L, 0)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("deductStock() - throws ResourceNotFoundException when no stock record found")
    void deductStock_throwsNotFound_whenNoStockRecord() {
        Warehouse w = buildWarehouse(1L, "WH1", true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                warehouseService.deductStock(new StockUpdateRequest(1L, 99L, 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deductStock() - publishes LOW_STOCK event when stock falls below threshold")
    void deductStock_publishesLowStockEvent() {
        Warehouse w = buildWarehouse(1L, "WH1", true);
        StockLevel stock = buildStock(1L, 1L, 10L, 12);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(stock));
        when(stockLevelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(stockEventPublisher).publishIfLowStock(any(), any(), any());

        // Deduct 10 → leaves 2, triggers alert
        warehouseService.deductStock(new StockUpdateRequest(1L, 10L, 10));

        verify(stockEventPublisher).publishIfLowStock(1L, 10L, 2);
    }

    // ─── transferStock() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("transferStock() - success when source has sufficient stock")
    void transferStock_success() {
        Warehouse from = buildWarehouse(1L, "WH-From", true);
        Warehouse to = buildWarehouse(2L, "WH-To", true);
        StockLevel source = buildStock(1L, 1L, 10L, 50);
        StockLevel dest = buildStock(2L, 2L, 10L, 10);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(from));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(to));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(source));
        when(stockLevelRepository.findByWarehouseIdAndProductId(2L, 10L))
                .thenReturn(Optional.of(dest));
        doNothing().when(stockEventPublisher).publishIfLowStock(any(), any(), any());

        StockTransferRequest req = new StockTransferRequest(1L, 2L, 10L, 20);
        warehouseService.transferStock(req);

        // source: 50 - 20 = 30
        assertThat(source.getQuantity()).isEqualTo(30);
        // dest: 10 + 20 = 30
        assertThat(dest.getQuantity()).isEqualTo(30);

        verify(stockLevelRepository, times(2)).save(any());
        verify(stockEventPublisher).publishIfLowStock(1L, 10L, 30);
    }

    @Test
    @DisplayName("transferStock() - creates new destination stock record when none exists")
    void transferStock_createsDestinationRecord_whenMissing() {
        Warehouse from = buildWarehouse(1L, "WH-From", true);
        Warehouse to = buildWarehouse(2L, "WH-To", true);
        StockLevel source = buildStock(1L, 1L, 10L, 50);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(from));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(to));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(source));
        when(stockLevelRepository.findByWarehouseIdAndProductId(2L, 10L))
                .thenReturn(Optional.empty()); // no existing dest record
        doNothing().when(stockEventPublisher).publishIfLowStock(any(), any(), any());

        warehouseService.transferStock(new StockTransferRequest(1L, 2L, 10L, 15));

        // source: 50 - 15 = 35
        assertThat(source.getQuantity()).isEqualTo(35);
        // new destination saved with quantity 15
        verify(stockLevelRepository, times(2)).save(argThat(s ->
                s.getQuantity() == 35 || s.getQuantity() == 15));
    }

    @Test
    @DisplayName("transferStock() - throws InsufficientStockException when source stock too low")
    void transferStock_throwsInsufficientStock() {
        Warehouse from = buildWarehouse(1L, "WH-From", true);
        Warehouse to = buildWarehouse(2L, "WH-To", true);
        StockLevel source = buildStock(1L, 1L, 10L, 5);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(from));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(to));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(source));

        assertThatThrownBy(() ->
                warehouseService.transferStock(new StockTransferRequest(1L, 2L, 10L, 100)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Available: 5");

        verify(stockLevelRepository, never()).save(any());
    }

    @Test
    @DisplayName("transferStock() - throws RuntimeException when same warehouse")
    void transferStock_throwsException_whenSameWarehouse() {
        assertThatThrownBy(() ->
                warehouseService.transferStock(new StockTransferRequest(1L, 1L, 10L, 5)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("transferStock() - throws RuntimeException when quantity is zero")
    void transferStock_throwsException_whenQuantityZero() {
        assertThatThrownBy(() ->
                warehouseService.transferStock(new StockTransferRequest(1L, 2L, 10L, 0)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("transferStock() - throws ResourceNotFoundException when source has no stock")
    void transferStock_throwsNotFound_whenNoSourceStock() {
        Warehouse from = buildWarehouse(1L, "WH-From", true);
        Warehouse to = buildWarehouse(2L, "WH-To", true);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(from));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(to));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                warehouseService.transferStock(new StockTransferRequest(1L, 2L, 10L, 5)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("transferStock() - publishes LOW_STOCK event for source after transfer")
    void transferStock_publishesLowStockEvent_forSource() {
        Warehouse from = buildWarehouse(1L, "WH-From", true);
        Warehouse to = buildWarehouse(2L, "WH-To", true);
        StockLevel source = buildStock(1L, 1L, 10L, 8);
        StockLevel dest = buildStock(2L, 2L, 10L, 100);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(from));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(to));
        when(stockLevelRepository.findByWarehouseIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(source));
        when(stockLevelRepository.findByWarehouseIdAndProductId(2L, 10L))
                .thenReturn(Optional.of(dest));
        doNothing().when(stockEventPublisher).publishIfLowStock(any(), any(), any());

        // Transfer 5 → source drops to 3, which should trigger alert
        warehouseService.transferStock(new StockTransferRequest(1L, 2L, 10L, 5));

        verify(stockEventPublisher).publishIfLowStock(1L, 10L, 3);
    }

    // ─── getStockByWarehouse() ────────────────────────────────────────────────

    @Test
    @DisplayName("getStockByWarehouse() - returns all stock for given warehouse")
    void getStockByWarehouse_returnsStock() {
        Warehouse w = buildWarehouse(1L, "WH1", true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(stockLevelRepository.findByWarehouseId(1L))
                .thenReturn(List.of(
                        buildStock(1L, 1L, 10L, 50),
                        buildStock(2L, 1L, 11L, 30)
                ));

        List<StockLevelResponse> result = warehouseService.getStockByWarehouse(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuantity()).isEqualTo(50);
        assertThat(result.get(1).getQuantity()).isEqualTo(30);
    }

    @Test
    @DisplayName("getStockByWarehouse() - throws ResourceNotFoundException when warehouse missing")
    void getStockByWarehouse_throwsNotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getStockByWarehouse(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}