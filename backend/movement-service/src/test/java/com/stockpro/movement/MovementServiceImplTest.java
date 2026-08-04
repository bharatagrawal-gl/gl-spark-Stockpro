package com.stockpro.movement;

import com.stockpro.movement.dto.MovementRequest;
import com.stockpro.movement.dto.MovementResponse;
import com.stockpro.movement.dto.TransferMovementRequest;
import com.stockpro.movement.entity.MovementType;
import com.stockpro.movement.entity.StockMovement;
import com.stockpro.movement.exception.ResourceNotFoundException;
import com.stockpro.movement.repository.StockMovementRepository;
import com.stockpro.movement.service.MovementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovementServiceImpl Tests")
class MovementServiceImplTest {

    @Mock
    private StockMovementRepository movementRepository;

    @InjectMocks
    private MovementServiceImpl movementService;

    private StockMovement sampleMovement;

    @BeforeEach
    void setUp() {
        sampleMovement = StockMovement.builder()
                .id(1L)
                .warehouseId(10L)
                .productId(20L)
                .movementType(MovementType.STOCK_IN)
                .quantity(50)
                .referenceId(100L)
                .referenceType("MANUAL")
                .notes("Initial stock")
                .performedBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── logMovement ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("logMovement: saves STOCK_IN movement and returns response")
    void logMovement_stockIn_success() {
        MovementRequest request = new MovementRequest();
        request.setWarehouseId(10L);
        request.setProductId(20L);
        request.setMovementType(MovementType.STOCK_IN);
        request.setQuantity(50);
        request.setPerformedBy(1L);
        request.setReferenceType("MANUAL");

        when(movementRepository.save(any(StockMovement.class))).thenReturn(sampleMovement);

        MovementResponse response = movementService.logMovement(request);

        assertThat(response.getMovementType()).isEqualTo(MovementType.STOCK_IN);
        assertThat(response.getQuantity()).isEqualTo(50);
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("logMovement: throws RuntimeException when type is TRANSFER")
    void logMovement_rejectsTransferType() {
        MovementRequest request = new MovementRequest();
        request.setMovementType(MovementType.TRANSFER);
        request.setQuantity(10);

        assertThatThrownBy(() -> movementService.logMovement(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("transfer");
    }

    @Test
    @DisplayName("logMovement: throws RuntimeException when quantity is zero")
    void logMovement_zeroQuantity() {
        MovementRequest request = new MovementRequest();
        request.setMovementType(MovementType.STOCK_IN);
        request.setQuantity(0);

        assertThatThrownBy(() -> movementService.logMovement(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("logMovement: throws RuntimeException when quantity is negative")
    void logMovement_negativeQuantity() {
        MovementRequest request = new MovementRequest();
        request.setMovementType(MovementType.STOCK_OUT);
        request.setQuantity(-5);

        assertThatThrownBy(() -> movementService.logMovement(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("positive");
    }

    // ── logTransfer ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("logTransfer: creates OUT and IN records for different warehouses")
    void logTransfer_success() {
        TransferMovementRequest request = new TransferMovementRequest();
        request.setFromWarehouseId(1L);
        request.setToWarehouseId(2L);
        request.setProductId(20L);
        request.setQuantity(30);
        request.setPerformedBy(1L);

        StockMovement outMovement = StockMovement.builder().id(1L).warehouseId(1L)
                .productId(20L).movementType(MovementType.TRANSFER).quantity(30)
                .notes("Transfer OUT to warehouse 2").performedBy(1L)
                .createdAt(LocalDateTime.now()).build();

        StockMovement inMovement = StockMovement.builder().id(2L).warehouseId(2L)
                .productId(20L).movementType(MovementType.TRANSFER).quantity(30)
                .notes("Transfer IN from warehouse 1").performedBy(1L)
                .createdAt(LocalDateTime.now()).build();

        when(movementRepository.save(any(StockMovement.class)))
                .thenReturn(outMovement)
                .thenReturn(inMovement);

        List<MovementResponse> responses = movementService.logTransfer(request);

        assertThat(responses).hasSize(2);
        verify(movementRepository, times(2)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("logTransfer: throws RuntimeException when source == destination")
    void logTransfer_sameWarehouse() {
        TransferMovementRequest request = new TransferMovementRequest();
        request.setFromWarehouseId(1L);
        request.setToWarehouseId(1L);
        request.setQuantity(10);

        assertThatThrownBy(() -> movementService.logTransfer(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("logTransfer: throws RuntimeException when quantity is zero or negative")
    void logTransfer_invalidQuantity() {
        TransferMovementRequest request = new TransferMovementRequest();
        request.setFromWarehouseId(1L);
        request.setToWarehouseId(2L);
        request.setQuantity(0);

        assertThatThrownBy(() -> movementService.logTransfer(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("positive");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById: returns movement when found")
    void getById_found() {
        when(movementRepository.findById(1L)).thenReturn(Optional.of(sampleMovement));

        MovementResponse response = movementService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById: throws ResourceNotFoundException when not found")
    void getById_notFound() {
        when(movementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movementService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll: returns all movements")
    void getAll_returnsList() {
        when(movementRepository.findAll()).thenReturn(List.of(sampleMovement));

        assertThat(movementService.getAll()).hasSize(1);
    }

    // ── getByWarehouse ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByWarehouse: returns movements for warehouse")
    void getByWarehouse_returnsList() {
        when(movementRepository.findByWarehouseId(10L)).thenReturn(List.of(sampleMovement));

        List<MovementResponse> result = movementService.getByWarehouse(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWarehouseId()).isEqualTo(10L);
    }

    // ── getByProduct ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByProduct: returns movements for product")
    void getByProduct_returnsList() {
        when(movementRepository.findByProductId(20L)).thenReturn(List.of(sampleMovement));

        List<MovementResponse> result = movementService.getByProduct(20L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(20L);
    }

    // ── getByType ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByType: returns movements matching type")
    void getByType_returnsMatching() {
        when(movementRepository.findByMovementType(MovementType.STOCK_IN))
                .thenReturn(List.of(sampleMovement));

        List<MovementResponse> result = movementService.getByType(MovementType.STOCK_IN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovementType()).isEqualTo(MovementType.STOCK_IN);
    }

    // ── getByDateRange ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByDateRange: returns movements in date range")
    void getByDateRange_success() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to   = LocalDateTime.now();

        when(movementRepository.findByCreatedAtBetween(from, to))
                .thenReturn(List.of(sampleMovement));

        List<MovementResponse> result = movementService.getByDateRange(from, to);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getByDateRange: throws RuntimeException when 'from' is after 'to'")
    void getByDateRange_invalidRange() {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to   = LocalDateTime.now().minusDays(1);

        assertThatThrownBy(() -> movementService.getByDateRange(from, to))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("before");
    }

    // ── getByWarehouseAndProduct ──────────────────────────────────────────────

    @Test
    @DisplayName("getByWarehouseAndProduct: returns combined filter results")
    void getByWarehouseAndProduct_returnsList() {
        when(movementRepository.findByWarehouseIdAndProductId(10L, 20L))
                .thenReturn(List.of(sampleMovement));

        List<MovementResponse> result = movementService.getByWarehouseAndProduct(10L, 20L);

        assertThat(result).hasSize(1);
    }
}
