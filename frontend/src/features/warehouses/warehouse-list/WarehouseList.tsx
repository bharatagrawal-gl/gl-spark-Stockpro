import { useEffect, useState } from 'react';
import { Button, Card, CircularProgress } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { Product } from '../../../core/models/product';
import { StockLevel, StockTransferRequest, StockUpdateRequest, Warehouse, WarehouseRequest } from '../../../core/models/warehouse';
import { authService } from '../../../core/services/auth';
import { productService } from '../../../core/services/product';
import { warehouseService } from '../../../core/services/warehouse';
import { useSnackbar, getErrorMessage } from '../../../shared/hooks/useSnackbar';
import { SnackbarHost } from '../../../shared/components/SnackbarHost';
import { WarehouseDialog } from './WarehouseDialog';
import { WarehouseStockDialog } from './WarehouseStockDialog';
import { WarehouseTransferDialog } from './WarehouseTransferDialog';
import './WarehouseList.css';

interface StockRow extends StockLevel {
  productName: string;
}

export function WarehouseList() {
  const [loading, setLoading] = useState(true);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [stockLevels, setStockLevels] = useState<StockLevel[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [productNameMap, setProductNameMap] = useState<Map<number, string>>(new Map());
  const [canWrite] = useState(authService.canWriteInventory());
  const [isAdmin] = useState(authService.isAdmin());

  const [warehouseDialogOpen, setWarehouseDialogOpen] = useState(false);
  const [editingWarehouse, setEditingWarehouse] = useState<Warehouse | null>(null);

  const [stockDialogState, setStockDialogState] = useState<{ mode: 'ADD' | 'DEDUCT'; warehouse: Warehouse } | null>(null);
  const [transferDialogWarehouse, setTransferDialogWarehouse] = useState<Warehouse | null>(null);

  const { snackbar, showMessage, close } = useSnackbar();

  useEffect(() => { loadData(); }, []);

  function loadData(): void {
    setLoading(true);
    Promise.all([
      warehouseService.getAll(),
      warehouseService.getAllStock(),
      productService.getAll()
    ])
      .then(([warehousesData, stockLevelsData, productsData]) => {
        setWarehouses(warehousesData);
        setStockLevels(stockLevelsData);
        setProducts(productsData);
        setProductNameMap(new Map(productsData.map((product) => [product.id, product.name])));
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to load warehouse data')))
      .finally(() => setLoading(false));
  }

  function getWarehouseStock(warehouseId: number): StockRow[] {
    return stockLevels
      .filter((stock) => stock.warehouseId === warehouseId)
      .map((stock) => ({ ...stock, productName: productNameMap.get(stock.productId) ?? `Product #${stock.productId}` }))
      .sort((first, second) => second.quantity - first.quantity);
  }

  function getWarehouseUnits(warehouseId: number): number {
    return getWarehouseStock(warehouseId).reduce((sum, stock) => sum + stock.quantity, 0);
  }

  function openWarehouseDialog(warehouse: Warehouse | null = null): void {
    if (!canWrite) return;
    setEditingWarehouse(warehouse);
    setWarehouseDialogOpen(true);
  }

  function handleSaveWarehouse(payload: WarehouseRequest): void {
    const request = editingWarehouse
      ? warehouseService.update(editingWarehouse.id, payload)
      : warehouseService.create(payload);

    request
      .then(() => {
        showMessage(`Warehouse ${editingWarehouse ? 'updated' : 'created'} successfully`);
        setWarehouseDialogOpen(false);
        loadData();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to save warehouse')));
  }

  function toggleStatus(warehouse: Warehouse): void {
    if (!canWrite) return;
    const request = warehouse.isActive
      ? warehouseService.deactivate(warehouse.id)
      : warehouseService.activate(warehouse.id);

    request
      .then(() => {
        showMessage(`Warehouse ${warehouse.isActive ? 'deactivated' : 'reactivated'}`);
        loadData();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to update warehouse status')));
  }

  function openStockDialog(mode: 'ADD' | 'DEDUCT', warehouse: Warehouse): void {
    if (!canWrite) return;
    setStockDialogState({ mode, warehouse });
  }

  function handleSaveStock(payload: StockUpdateRequest): void {
    if (!stockDialogState) return;
    const mode = stockDialogState.mode;
    const request = mode === 'ADD' ? warehouseService.addStock(payload) : warehouseService.deductStock(payload);

    request
      .then(() => {
        showMessage(`Stock ${mode === 'ADD' ? 'added' : 'deducted'} successfully`);
        setStockDialogState(null);
        loadData();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to update stock')));
  }

  function openTransferDialog(warehouse: Warehouse): void {
    if (!canWrite) return;
    setTransferDialogWarehouse(warehouse);
  }

  function handleSaveTransfer(payload: StockTransferRequest): void {
    warehouseService.transferStock(payload)
      .then(() => {
        showMessage('Stock transferred successfully');
        setTransferDialogWarehouse(null);
        loadData();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to transfer stock')));
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Warehouses</h1>
          <p>Track capacity and move stock between locations.</p>
        </div>

        {isAdmin && (
          <Button variant="contained" color="primary" startIcon={<AddIcon />} onClick={() => openWarehouseDialog()}>
            New Warehouse
          </Button>
        )}
      </div>

      {loading && (
        <div className="loading-state">
          <CircularProgress size={44} />
        </div>
      )}

      {!loading && (
        <div className="warehouse-grid">
          {warehouses.map((warehouse) => {
            const stock = getWarehouseStock(warehouse.id);
            return (
              <Card key={warehouse.id} className="warehouse-card">
                <div className="card-top">
                  <div>
                    <h2>{warehouse.name}</h2>
                    <p>{warehouse.location}</p>
                  </div>
                  <span className={`status-chip ${(warehouse.isActive ? 'ACTIVE' : 'INACTIVE').toLowerCase()}`}>
                    {warehouse.isActive ? 'ACTIVE' : 'INACTIVE'}
                  </span>
                </div>

                <div className="meta-grid">
                  <div>
                    <span>Capacity</span>
                    <strong>{warehouse.capacity}</strong>
                  </div>
                  <div>
                    <span>Total Units</span>
                    <strong>{getWarehouseUnits(warehouse.id)}</strong>
                  </div>
                  <div>
                    <span>Products</span>
                    <strong>{stock.length}</strong>
                  </div>
                </div>

                {canWrite && (
                  <div className="actions">
                    <Button variant="outlined" onClick={() => openWarehouseDialog(warehouse)}>Edit</Button>
                    <Button variant="outlined" onClick={() => toggleStatus(warehouse)}>
                      {warehouse.isActive ? 'Deactivate' : 'Activate'}
                    </Button>
                    <Button variant="outlined" onClick={() => openStockDialog('ADD', warehouse)}>Add Stock</Button>
                    <Button variant="outlined" onClick={() => openStockDialog('DEDUCT', warehouse)}>Deduct</Button>
                    <Button variant="contained" color="primary" onClick={() => openTransferDialog(warehouse)}>Transfer</Button>
                  </div>
                )}

                <div className="stock-panel">
                  <div className="stock-header">
                    <h3>Current Stock</h3>
                    <span>{stock.length} items</span>
                  </div>

                  {stock.length === 0 && (
                    <div className="empty-state">No stock recorded for this warehouse yet.</div>
                  )}

                  {stock.map((row) => (
                    <div key={row.id} className="stock-row">
                      <div>
                        <strong>{row.productName}</strong>
                        <span>Product #{row.productId}</span>
                      </div>
                      <span>{row.quantity}</span>
                    </div>
                  ))}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {warehouseDialogOpen && (
        <WarehouseDialog
          open={warehouseDialogOpen}
          warehouse={editingWarehouse}
          onClose={() => setWarehouseDialogOpen(false)}
          onSave={handleSaveWarehouse}
        />
      )}

      {stockDialogState && (
        <WarehouseStockDialog
          open={!!stockDialogState}
          mode={stockDialogState.mode}
          warehouse={stockDialogState.warehouse}
          products={products}
          onClose={() => setStockDialogState(null)}
          onSave={handleSaveStock}
        />
      )}

      {transferDialogWarehouse && (
        <WarehouseTransferDialog
          open={!!transferDialogWarehouse}
          warehouse={transferDialogWarehouse}
          products={products}
          warehouses={warehouses}
          onClose={() => setTransferDialogWarehouse(null)}
          onSave={handleSaveTransfer}
        />
      )}

      <SnackbarHost snackbar={snackbar} onClose={close} />
    </div>
  );
}
