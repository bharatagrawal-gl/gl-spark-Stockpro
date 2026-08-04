package com.stockpro.purchase;

import com.stockpro.purchase.dto.*;
import com.stockpro.purchase.entity.*;
import com.stockpro.purchase.event.PurchaseEventPublisher;
import com.stockpro.purchase.exception.InvalidStatusTransitionException;
import com.stockpro.purchase.exception.ResourceNotFoundException;
import com.stockpro.purchase.repository.PurchaseOrderItemRepository;
import com.stockpro.purchase.repository.PurchaseOrderRepository;
import com.stockpro.purchase.service.PurchaseOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderServiceImpl Tests")
class PurchaseOrderServiceImplTest {

    @Mock private PurchaseOrderRepository poRepository;
    @Mock private PurchaseOrderItemRepository itemRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private PurchaseEventPublisher eventPublisher;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    private PurchaseOrder draftPo;
    private PurchaseOrderItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = PurchaseOrderItem.builder()
                .id(1L).productId(10L).warehouseId(1L)
                .quantityOrdered(50).quantityReceived(0)
                .unitPrice(new BigDecimal("20.00"))
                .createdAt(LocalDateTime.now())
                .build();

        draftPo = PurchaseOrder.builder()
                .id(1L).supplierId(5L).orderedBy(1L)
                .status(PoStatus.DRAFT)
                .expectedDate(LocalDate.now().plusDays(7))
                .items(new ArrayList<>(List.of(sampleItem)))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        sampleItem.setPurchaseOrder(draftPo);
    }

    // ── createPurchaseOrder ───────────────────────────────────────────────────

    @Test
    @DisplayName("createPurchaseOrder: creates PO with items and returns DRAFT status")
    void createPurchaseOrder_success() {
        PurchaseOrderItemRequest itemReq = new PurchaseOrderItemRequest();
        itemReq.setProductId(10L);
        itemReq.setWarehouseId(1L);
        itemReq.setQuantityOrdered(50);
        itemReq.setUnitPrice(new BigDecimal("20.00"));

        PurchaseOrderRequest req = new PurchaseOrderRequest();
        req.setSupplierId(5L);
        req.setOrderedBy(1L);
        req.setItems(List.of(itemReq));
        req.setExpectedDate(LocalDate.now().plusDays(7));

        when(poRepository.save(any(PurchaseOrder.class))).thenReturn(draftPo);

        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(req);

        assertThat(response.getStatus()).isEqualTo(PoStatus.DRAFT);
        assertThat(response.getItems()).hasSize(1);
        verify(poRepository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("createPurchaseOrder: throws RuntimeException when items list is empty")
    void createPurchaseOrder_emptyItems() {
        PurchaseOrderRequest req = new PurchaseOrderRequest();
        req.setSupplierId(5L);
        req.setOrderedBy(1L);
        req.setItems(List.of());

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("item");
    }

    @Test
    @DisplayName("createPurchaseOrder: throws RuntimeException when items is null")
    void createPurchaseOrder_nullItems() {
        PurchaseOrderRequest req = new PurchaseOrderRequest();
        req.setSupplierId(5L);
        req.setOrderedBy(1L);
        req.setItems(null);

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(req))
                .isInstanceOf(RuntimeException.class);
    }

    // ── getPurchaseOrderById ──────────────────────────────────────────────────

    @Test
    @DisplayName("getPurchaseOrderById: returns PO when found")
    void getPurchaseOrderById_found() {
        when(poRepository.findById(1L)).thenReturn(Optional.of(draftPo));

        PurchaseOrderResponse response = purchaseOrderService.getPurchaseOrderById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getPurchaseOrderById: throws ResourceNotFoundException when not found")
    void getPurchaseOrderById_notFound() {
        when(poRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseOrderService.getPurchaseOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getByStatus ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByStatus: returns POs with matching status")
    void getByStatus_returnsDrafts() {
        when(poRepository.findByStatus(PoStatus.DRAFT)).thenReturn(List.of(draftPo));

        List<PurchaseOrderResponse> result = purchaseOrderService.getByStatus(PoStatus.DRAFT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(PoStatus.DRAFT);
    }

    // ── getBySupplier ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBySupplier: returns POs for supplier")
    void getBySupplier_returnsList() {
        when(poRepository.findBySupplierId(5L)).thenReturn(List.of(draftPo));

        List<PurchaseOrderResponse> result = purchaseOrderService.getBySupplier(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSupplierId()).isEqualTo(5L);
    }

    // ── approvePurchaseOrder ──────────────────────────────────────────────────

    @Test
    @DisplayName("approvePurchaseOrder: transitions DRAFT to APPROVED")
    void approvePurchaseOrder_success() {
        ApproveRequest approveReq = new ApproveRequest();
        approveReq.setApprovedBy(2L);

        PurchaseOrder approved = PurchaseOrder.builder()
                .id(1L).supplierId(5L).orderedBy(1L).approvedBy(2L)
                .status(PoStatus.APPROVED)
                .expectedDate(LocalDate.now().plusDays(7))
                .items(new ArrayList<>(List.of(sampleItem)))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(poRepository.findById(1L)).thenReturn(Optional.of(draftPo));
        when(poRepository.save(any(PurchaseOrder.class))).thenReturn(approved);

        PurchaseOrderResponse response = purchaseOrderService.approvePurchaseOrder(1L, approveReq);

        assertThat(response.getStatus()).isEqualTo(PoStatus.APPROVED);
        assertThat(response.getApprovedBy()).isEqualTo(2L);
    }

    @Test
    @DisplayName("approvePurchaseOrder: throws InvalidStatusTransitionException when not DRAFT")
    void approvePurchaseOrder_notDraft() {
        draftPo.setStatus(PoStatus.APPROVED);
        ApproveRequest req = new ApproveRequest();
        req.setApprovedBy(2L);

        when(poRepository.findById(1L)).thenReturn(Optional.of(draftPo));

        assertThatThrownBy(() -> purchaseOrderService.approvePurchaseOrder(1L, req))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("DRAFT");
    }

    // ── cancelPurchaseOrder ───────────────────────────────────────────────────

    @Test
    @DisplayName("cancelPurchaseOrder: cancels DRAFT order")
    void cancelPurchaseOrder_fromDraft() {
        PurchaseOrder cancelled = PurchaseOrder.builder()
                .id(1L).supplierId(5L).orderedBy(1L)
                .status(PoStatus.CANCELLED)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(poRepository.findById(1L)).thenReturn(Optional.of(draftPo));
        when(poRepository.save(any(PurchaseOrder.class))).thenReturn(cancelled);

        PurchaseOrderResponse response = purchaseOrderService.cancelPurchaseOrder(1L);

        assertThat(response.getStatus()).isEqualTo(PoStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelPurchaseOrder: throws InvalidStatusTransitionException when already RECEIVED")
    void cancelPurchaseOrder_alreadyReceived() {
        draftPo.setStatus(PoStatus.RECEIVED);
        when(poRepository.findById(1L)).thenReturn(Optional.of(draftPo));

        assertThatThrownBy(() -> purchaseOrderService.cancelPurchaseOrder(1L))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("RECEIVED");
    }

    @Test
    @DisplayName("cancelPurchaseOrder: throws InvalidStatusTransitionException when already CANCELLED")
    void cancelPurchaseOrder_alreadyCancelled() {
        draftPo.setStatus(PoStatus.CANCELLED);
        when(poRepository.findById(1L)).thenReturn(Optional.of(draftPo));

        assertThatThrownBy(() -> purchaseOrderService.cancelPurchaseOrder(1L))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("CANCELLED");
    }

    // ── receiveGoods ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("receiveGoods: transitions APPROVED to RECEIVED and calls warehouse")
    void receiveGoods_success() {
        draftPo.setStatus(PoStatus.APPROVED);

        ReceiveGoodsRequest req = new ReceiveGoodsRequest();
        req.setPerformedBy(3L);

        PurchaseOrder received = PurchaseOrder.builder()
                .id(1L).supplierId(5L).orderedBy(1L)
                .status(PoStatus.RECEIVED)
                .receivedDate(LocalDateTime.now())
                .receivedBy(3L)
                .items(new ArrayList<>(List.of(sampleItem)))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(poRepository.findById(1L)).thenReturn(Optional.of(draftPo));
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class))).thenReturn(null);
        when(poRepository.save(any(PurchaseOrder.class))).thenReturn(received);

        PurchaseOrderResponse response = purchaseOrderService.receiveGoods(1L, req);

        assertThat(response.getStatus()).isEqualTo(PoStatus.RECEIVED);
        assertThat(response.getReceivedBy()).isEqualTo(3L);
        verify(restTemplate, atLeastOnce()).postForObject(anyString(), any(), eq(Object.class));
    }

    @Test
    @DisplayName("receiveGoods: throws InvalidStatusTransitionException when not APPROVED")
    void receiveGoods_notApproved() {
        // draftPo is DRAFT status
        ReceiveGoodsRequest req = new ReceiveGoodsRequest();
        req.setPerformedBy(3L);

        when(poRepository.findById(1L)).thenReturn(Optional.of(draftPo));

        assertThatThrownBy(() -> purchaseOrderService.receiveGoods(1L, req))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    @DisplayName("receiveGoods: throws RuntimeException when warehouse call fails")
    void receiveGoods_warehouseCallFails() {
        draftPo.setStatus(PoStatus.APPROVED);

        ReceiveGoodsRequest req = new ReceiveGoodsRequest();
        req.setPerformedBy(3L);

        when(poRepository.findById(1L)).thenReturn(Optional.of(draftPo));
        when(restTemplate.postForObject(contains("warehouse"), any(), eq(Object.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> purchaseOrderService.receiveGoods(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Warehouse stock update failed");
    }
}
