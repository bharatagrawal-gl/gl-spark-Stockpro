import { useMemo, useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  TextField
} from '@mui/material';
import { Product } from '../../../core/models/product';
import { StockTransferRequest, Warehouse } from '../../../core/models/warehouse';

interface WarehouseTransferDialogProps {
  open: boolean;
  warehouse: Warehouse;
  products: Product[];
  warehouses: Warehouse[];
  onClose: () => void;
  onSave: (payload: StockTransferRequest) => void;
}

export function WarehouseTransferDialog({ open, warehouse, products, warehouses, onClose, onSave }: WarehouseTransferDialogProps) {
  const [productId, setProductId] = useState(0);
  const [toWarehouseId, setToWarehouseId] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [touched, setTouched] = useState(false);

  const destinationWarehouses = useMemo(
    () => warehouses.filter((w) => w.id !== warehouse.id),
    [warehouses, warehouse]
  );

  const isInvalid = productId < 1 || toWarehouseId < 1 || quantity < 1;

  function save(): void {
    if (isInvalid) {
      setTouched(true);
      return;
    }
    onSave({ fromWarehouseId: warehouse.id, toWarehouseId, productId, quantity });
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Transfer Stock</DialogTitle>
      <DialogContent>
        <div className="dialog-form">
          <TextField
            variant="outlined"
            select
            label="Product"
            value={productId}
            onChange={(e) => setProductId(Number(e.target.value))}
            error={touched && productId < 1}
          >
            {products.map((product) => (
              <MenuItem key={product.id} value={product.id}>
                {product.name} ({product.sku})
              </MenuItem>
            ))}
          </TextField>
          <TextField
            variant="outlined"
            select
            label="Destination Warehouse"
            value={toWarehouseId}
            onChange={(e) => setToWarehouseId(Number(e.target.value))}
            error={touched && toWarehouseId < 1}
          >
            {destinationWarehouses.map((w) => (
              <MenuItem key={w.id} value={w.id}>{w.name}</MenuItem>
            ))}
          </TextField>
          <TextField
            variant="outlined"
            label="Quantity"
            type="number"
            inputProps={{ min: 1 }}
            value={quantity}
            onChange={(e) => setQuantity(Number(e.target.value))}
            error={touched && quantity < 1}
          />
        </div>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" color="primary" disabled={isInvalid} onClick={save}>
          Transfer
        </Button>
      </DialogActions>
    </Dialog>
  );
}
