package com.stockpro.purchase.service;

import com.stockpro.purchase.dto.*;
import com.stockpro.purchase.entity.PoStatus;
import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);
    PurchaseOrderResponse getPurchaseOrderById(Long id);
    List<PurchaseOrderResponse> getAllPurchaseOrders();
    List<PurchaseOrderResponse> getByStatus(PoStatus status);
    List<PurchaseOrderResponse> getBySupplier(Long supplierId);
    PurchaseOrderResponse approvePurchaseOrder(Long id, ApproveRequest request);
    PurchaseOrderResponse cancelPurchaseOrder(Long id);
    PurchaseOrderResponse receiveGoods(Long id, ReceiveGoodsRequest request);
}