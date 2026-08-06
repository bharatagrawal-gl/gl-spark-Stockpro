import { useState } from 'react';
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
import { StockUpdateRequest, Warehouse } from '../../../core/models/warehouse';

interface WarehouseStockDialogProps {
  open: boolean;
  mode: 'ADD' | 'DEDUCT';
  warehouse: Warehouse;
  products: Product[];
  onClose: () => void;
  onSave: (payload: StockUpdateRequest) => void;
}

export function WarehouseStockDialog({ open, mode, warehouse, products, onClose, onSave }: WarehouseStockDialogProps) {
  const [productId, setProductId] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [touched, setTouched] = useState(false);

  const isInvalid = productId < 1 || quantity < 1;

  function save(): void {
    if (isInvalid) {
      setTouched(true);
      return;
    }
    onSave({ warehouseId: warehouse.id, productId, quantity });
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{mode === 'ADD' ? 'Add Stock' : 'Deduct Stock'}</DialogTitle>
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
          Submit
        </Button>
      </DialogActions>
    </Dialog>
  );
}
