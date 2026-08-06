import { useEffect, useMemo, useState } from 'react';
import {
  Button,
  Card,
  CircularProgress,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Tooltip
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { Product } from '../../../core/models/product';
import { ApproveRequest, PoStatus, PurchaseOrder, PurchaseOrderRequest, ReceiveGoodsRequest } from '../../../core/models/purchase-order';
import { Supplier } from '../../../core/models/supplier';
import { User } from '../../../core/models/user';
import { Warehouse } from '../../../core/models/warehouse';
import { authService } from '../../../core/services/auth';
import { productService } from '../../../core/services/product';
import { purchaseOrderService } from '../../../core/services/purchase-order';
import { supplierService } from '../../../core/services/supplier';
import { warehouseService } from '../../../core/services/warehouse';
import { useSnackbar, getErrorMessage } from '../../../shared/hooks/useSnackbar';
import { SnackbarHost } from '../../../shared/components/SnackbarHost';
import { formatCurrencyINR, formatMediumDate } from '../../../shared/utils/format';
import { PoDialog } from './PoDialog';
import './PoList.css';

type StatusFilter = 'ALL' | PoStatus | 'PENDING_APPROVAL';

export function PoList() {
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrder[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [canWrite] = useState(authService.canWriteInventory());
  const [dialogOpen, setDialogOpen] = useState(false);
  const { snackbar, showMessage, close } = useSnackbar();

  useEffect(() => { loadData(); }, []);

  function resolveCurrentUserId(users: User[]): number | null {
    const storedId = Number(localStorage.getItem('userId'));
    if (Number.isFinite(storedId) && storedId > 0) {
      return storedId;
    }
    const email = localStorage.getItem('email');
    return users.find((user) => user.email === email)?.id ?? null;
  }

  function loadData(): void {
    setLoading(true);
    Promise.all([
      purchaseOrderService.getAll(),
      supplierService.getAll(),
      productService.getAll(),
      warehouseService.getAll(),
      canWrite ? authService.getAllUsers().catch(() => [] as User[]) : Promise.resolve([] as User[])
    ])
      .then(([purchaseOrdersData, suppliersData, productsData, warehousesData, users]) => {
        setPurchaseOrders(purchaseOrdersData);
        setSuppliers(suppliersData);
        setProducts(productsData);
        setWarehouses(warehousesData);
        setCurrentUserId(resolveCurrentUserId(users));
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to load purchase orders')))
      .finally(() => setLoading(false));
  }

  const filteredPurchaseOrders = useMemo(
    () => purchaseOrders.filter((po) => statusFilter === 'ALL' || po.status === statusFilter),
    [purchaseOrders, statusFilter]
  );

  function getSupplierName(id: number): string {
    return suppliers.find((supplier) => supplier.id === id)?.name ?? `Supplier #${id}`;
  }

  function getProductName(id: number): string {
    return products.find((product) => product.id === id)?.name ?? `Product #${id}`;
  }

  function getWarehouseName(id: number): string {
    return warehouses.find((warehouse) => warehouse.id === id)?.name ?? `Warehouse #${id}`;
  }

  function getOrderTotal(po: PurchaseOrder): number {
    return po.items.reduce((sum, item) => sum + (item.quantityOrdered * item.unitPrice), 0);
  }

  function getVisibleItems(po: PurchaseOrder) {
    return po.items.slice(0, 2);
  }

  function openCreateDialog(): void {
    if (!canWrite) return;
    if (!currentUserId) {
      showMessage('Current user could not be resolved for purchase order actions.');
      return;
    }
    setDialogOpen(true);
  }

  function handleSave(payload: PurchaseOrderRequest): void {
    purchaseOrderService.create(payload)
      .then(() => {
        showMessage('Purchase order created successfully');
        setDialogOpen(false);
        loadData();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to create purchase order')));
  }

  function approve(po: PurchaseOrder): void {
    if (!canWrite) return;
    if (!currentUserId) {
      showMessage('Current user could not be resolved for approval.');
      return;
    }
    const payload: ApproveRequest = { approvedBy: currentUserId };
    purchaseOrderService.approve(po.id, payload)
      .then(() => {
        showMessage(`PO #${po.id} approved`);
        loadData();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to approve purchase order')));
  }

  function receive(po: PurchaseOrder): void {
    if (!canWrite) return;
    if (!currentUserId) {
      showMessage('Current user could not be resolved for receiving goods.');
      return;
    }
    const payload: ReceiveGoodsRequest = { performedBy: currentUserId };
    purchaseOrderService.receiveGoods(po.id, payload)
      .then(() => {
        showMessage(`Goods received for PO #${po.id}`);
        loadData();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to receive goods')));
  }

  function cancel(po: PurchaseOrder): void {
    if (!canWrite) return;
    purchaseOrderService.cancel(po.id)
      .then(() => {
        showMessage(`PO #${po.id} cancelled`);
        loadData();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to cancel purchase order')));
  }

  function canApprove(po: PurchaseOrder): boolean {
    return authService.isAdmin() && (po.status === 'DRAFT' || po.status === 'PENDING_APPROVAL');
  }

  function canReceive(po: PurchaseOrder): boolean {
    return canWrite && (po.status === 'APPROVED' || po.status === 'PARTIALLY_RECEIVED');
  }

  function canCancel(po: PurchaseOrder): boolean {
    return canWrite && !['RECEIVED', 'CANCELLED'].includes(po.status);
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Purchase Orders</h1>
          <p>Create, approve, receive, and monitor order flow.</p>
        </div>

        {canWrite && (
          <Button variant="contained" color="primary" startIcon={<AddIcon />} onClick={openCreateDialog}>
            <Tooltip title="PO = Purchase Order"><span>New PO</span></Tooltip>
          </Button>
        )}
      </div>

      <Card>
        <div className="toolbar">
          <TextField
            className="field"
            variant="outlined"
            select
            label="Status"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
          >
            <MenuItem value="ALL">All statuses</MenuItem>
            <MenuItem value="DRAFT">Draft</MenuItem>
            <MenuItem value="PENDING_APPROVAL">Pending Approval</MenuItem>
            <MenuItem value="APPROVED">Approved</MenuItem>
            <MenuItem value="PARTIALLY_RECEIVED">Partially Received</MenuItem>
            <MenuItem value="RECEIVED">Received</MenuItem>
            <MenuItem value="CANCELLED">Cancelled</MenuItem>
          </TextField>
        </div>

        {loading && (
          <div className="loading-state">
            <CircularProgress size={42} />
          </div>
        )}

        {!loading && (
          <div className="table-shell">
            <Table className="mat-card-table">
              <TableHead>
                <TableRow>
                  <TableCell><Tooltip title="PO = Purchase Order"><span>PO</span></Tooltip></TableCell>
                  <TableCell>Supplier</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Expected</TableCell>
                  <TableCell>Items</TableCell>
                  <TableCell>Total</TableCell>
                  {canWrite && <TableCell />}
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredPurchaseOrders.map((po) => (
                  <TableRow key={po.id}>
                    <TableCell>
                      <div className="primary-cell">
                        <strong>#{po.id}</strong>
                        <span>{formatMediumDate(po.createdAt)}</span>
                      </div>
                    </TableCell>
                    <TableCell>{getSupplierName(po.supplierId)}</TableCell>
                    <TableCell>
                      <span className={`status-chip ${po.status.toLowerCase()}`}>{po.status}</span>
                    </TableCell>
                    <TableCell>{formatMediumDate(po.expectedDate)}</TableCell>
                    <TableCell>
                      <div className="item-list">
                        {getVisibleItems(po).map((item, index) => (
                          <span key={index}>
                            {getProductName(item.productId)} to {getWarehouseName(item.warehouseId)}
                          </span>
                        ))}
                        {po.items.length > 2 && <span>+{po.items.length - 2} more</span>}
                      </div>
                    </TableCell>
                    <TableCell>{formatCurrencyINR(getOrderTotal(po))}</TableCell>
                    {canWrite && (
                      <TableCell>
                        <div className="row-actions">
                          {canApprove(po) && <Button variant="outlined" onClick={() => approve(po)}>Approve</Button>}
                          {canReceive(po) && <Button variant="outlined" onClick={() => receive(po)}>Receive</Button>}
                          {canCancel(po) && <Button color="warning" onClick={() => cancel(po)}>Cancel</Button>}
                        </div>
                      </TableCell>
                    )}
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {filteredPurchaseOrders.length === 0 && (
              <div className="empty-state">No purchase orders found for the selected status.</div>
            )}
          </div>
        )}
      </Card>

      {dialogOpen && currentUserId && (
        <PoDialog
          open={dialogOpen}
          suppliers={suppliers.filter((supplier) => supplier.isActive)}
          products={products.filter((product) => product.isActive)}
          warehouses={warehouses.filter((warehouse) => warehouse.isActive)}
          currentUserId={currentUserId}
          onClose={() => setDialogOpen(false)}
          onSave={handleSave}
        />
      )}

      <SnackbarHost snackbar={snackbar} onClose={close} />
    </div>
  );
}
