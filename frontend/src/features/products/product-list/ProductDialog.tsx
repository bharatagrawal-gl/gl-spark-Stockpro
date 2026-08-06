import { useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField
} from '@mui/material';
import { Product, ProductRequest } from '../../../core/models/product';

interface ProductDialogProps {
  open: boolean;
  product: Product | null;
  onClose: () => void;
  onSave: (payload: ProductRequest) => void;
}

function formatIndianCurrencyWords(value: number): string {
  if (!value || value <= 0) return '';
  if (value >= 10000000) {
    return `₹${(value / 10000000).toLocaleString('en-IN', { maximumFractionDigits: 2 })} Crore`;
  }
  if (value >= 100000) {
    return `₹${(value / 100000).toLocaleString('en-IN', { maximumFractionDigits: 2 })} Lakh`;
  }
  if (value >= 1000) {
    return `₹${(value / 1000).toLocaleString('en-IN', { maximumFractionDigits: 2 })} Thousand`;
  }
  return `₹${value.toLocaleString('en-IN')}`;
}

export function ProductDialog({ open, product, onClose, onSave }: ProductDialogProps) {
  const [name, setName] = useState(product?.name ?? '');
  const [sku, setSku] = useState(product?.sku ?? '');
  const [barcode, setBarcode] = useState(product?.barcode ?? '');
  const [price, setPrice] = useState(product?.price ?? 0);
  const [reorderLevel, setReorderLevel] = useState(product?.reorderLevel ?? 0);
  const [touched, setTouched] = useState(false);

  const isInvalid = !name || !sku || !barcode || price < 0 || reorderLevel < 0;

  function save(): void {
    if (isInvalid) {
      setTouched(true);
      return;
    }
    onSave({ name, sku, barcode, price, reorderLevel });
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{product ? 'Edit Product' : 'Add Product'}</DialogTitle>
      <DialogContent>
        <div className="dialog-form">
          <TextField
            variant="outlined"
            label="Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            error={touched && !name}
          />
          <TextField
            variant="outlined"
            label="SKU"
            value={sku}
            onChange={(e) => setSku(e.target.value)}
            error={touched && !sku}
          />
          <TextField
            variant="outlined"
            label="Barcode"
            value={barcode}
            onChange={(e) => setBarcode(e.target.value)}
            error={touched && !barcode}
          />
          <TextField
            variant="outlined"
            label="Price"
            type="number"
            inputProps={{ min: 0 }}
            value={price}
            onChange={(e) => setPrice(Number(e.target.value))}
            error={touched && price < 0}
            helperText={price > 0 ? formatIndianCurrencyWords(price) : ''}
          />
          <TextField
            variant="outlined"
            label="Reorder Level"
            type="number"
            inputProps={{ min: 0 }}
            value={reorderLevel}
            onChange={(e) => setReorderLevel(Number(e.target.value))}
            error={touched && reorderLevel < 0}
          />
        </div>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" color="primary" disabled={isInvalid} onClick={save}>
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
}
