import { useEffect, useMemo, useState } from 'react';
import {
  Card,
  CircularProgress,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Button
} from '@mui/material';
import { MovementRequest, MovementType, StockMovement } from '../../../core/models/movement';
import { Product } from '../../../core/models/product';
import { Warehouse } from '../../../core/models/warehouse';
import { movementService } from '../../../core/services/movement';
import { productService } from '../../../core/services/product';
import { warehouseService } from '../../../core/services/warehouse';
import { useSnackbar, getErrorMessage } from '../../../shared/hooks/useSnackbar';
import { SnackbarHost } from '../../../shared/components/SnackbarHost';
import { formatMediumDateTime } from '../../../shared/utils/format';
import { authService } from '../../../core/services/auth';
import { MovementDialog } from './MovementDialog';
import './MovementList.css';

type TypeFilter = 'ALL' | MovementType;

export function MovementList() {
  const [loading, setLoading] = useState(true);
  const [movements, setMovements] = useState<StockMovement[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('ALL');
  const [productFilter, setProductFilter] = useState(0);
  const [warehouseFilter, setWarehouseFilter] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const canWrite = authService.canWriteInventory();
  const canCreateMovement = authService.canCreateMovement();
  const { snackbar, showMessage, close } = useSnackbar();

  useEffect(() => { loadData(); }, []);

  function loadData(): void {
    setLoading(true);
    Promise.all([
      movementService.getAll(),
      productService.getAll(),
      warehouseService.getAll()
    ])
      .then(([movementsData, productsData, warehousesData]) => {
        setMovements(movementsData);
        setProducts(productsData);
        setWarehouses(warehousesData);
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to load movements')))
      .finally(() => setLoading(false));
  }

  const filteredMovements = useMemo(() => movements.filter((movement) =>
    (typeFilter === 'ALL' || movement.movementType === typeFilter) &&
    (!productFilter || movement.productId === productFilter) &&
    (!warehouseFilter || movement.warehouseId === warehouseFilter)
  ), [movements, typeFilter, productFilter, warehouseFilter]);

  function getProductName(id: number): string {
    return products.find((product) => product.id === id)?.name ?? `Product #${id}`;
  }

  function getWarehouseName(id: number): string {
    return warehouses.find((warehouse) => warehouse.id === id)?.name ?? `Warehouse #${id}`;
  }

  function handleSaveMovement(payload: MovementRequest): void {
    const stockReq = {
      warehouseId: payload.warehouseId,
      productId: payload.productId,
      quantity: payload.quantity
    };

    const action = payload.movementType === 'STOCK_IN' 
      ? warehouseService.addStock(stockReq) 
      : warehouseService.deductStock(stockReq);

    action
      .then(() => {
        showMessage('Stock movement recorded successfully');
        setDialogOpen(false);
        loadData();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Failed to record stock movement')));
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Stock Movements</h1>
          <p>Review inbound and outbound stock activity across warehouses.</p>
        </div>
        {canCreateMovement && (
          <Button variant="contained" color="primary" onClick={() => setDialogOpen(true)}>
            + NEW MOVEMENT
          </Button>
        )}
      </div>

      <Card>
        <div className="toolbar">
          <TextField
            className="field"
            variant="outlined"
            select
            label="Type"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value as TypeFilter)}
          >
            <MenuItem value="ALL">All types</MenuItem>
            <MenuItem value="STOCK_IN">Stock In</MenuItem>
            <MenuItem value="STOCK_OUT">Stock Out</MenuItem>
          </TextField>

          <TextField
            className="field"
            variant="outlined"
            select
            label="Product"
            value={productFilter}
            onChange={(e) => setProductFilter(Number(e.target.value))}
          >
            <MenuItem value={0}>All products</MenuItem>
            {products.map((product) => (
              <MenuItem key={product.id} value={product.id}>{product.name}</MenuItem>
            ))}
          </TextField>

          <TextField
            className="field"
            variant="outlined"
            select
            label="Warehouse"
            value={warehouseFilter}
            onChange={(e) => setWarehouseFilter(Number(e.target.value))}
          >
            <MenuItem value={0}>All warehouses</MenuItem>
            {warehouses.map((warehouse) => (
              <MenuItem key={warehouse.id} value={warehouse.id}>{warehouse.name}</MenuItem>
            ))}
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
                  <TableCell>Date</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Product</TableCell>
                  <TableCell>Warehouse</TableCell>
                  <TableCell>Quantity</TableCell>
                  <TableCell>Reference</TableCell>
                  <TableCell>Performed By</TableCell>
                  <TableCell>Notes</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredMovements.map((movement) => (
                  <TableRow key={movement.id}>
                    <TableCell>{formatMediumDateTime(movement.createdAt)}</TableCell>
                    <TableCell>
                      <span className={`status-chip ${movement.movementType.toLowerCase()}`}>{movement.movementType}</span>
                    </TableCell>
                    <TableCell>{getProductName(movement.productId)}</TableCell>
                    <TableCell>{getWarehouseName(movement.warehouseId)}</TableCell>
                    <TableCell>{movement.quantity}</TableCell>
                    <TableCell>{movement.referenceType} #{movement.referenceId}</TableCell>
                    <TableCell>User #{movement.performedBy}</TableCell>
                    <TableCell>{movement.notes || '-'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {filteredMovements.length === 0 && (
              <div className="empty-state">No movements match the selected filters.</div>
            )}
          </div>
        )}
      </Card>

      <SnackbarHost snackbar={snackbar} onClose={close} />
      
      {dialogOpen && (
        <MovementDialog
          open={dialogOpen}
          products={products}
          warehouses={warehouses}
          onClose={() => setDialogOpen(false)}
          onSave={handleSaveMovement}
        />
      )}
    </div>
  );
}
