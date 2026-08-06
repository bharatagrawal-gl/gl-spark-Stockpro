import { useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  MenuItem,
  TextField
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { Product } from '../../../core/models/product';
import { PurchaseOrderItemRequest, PurchaseOrderRequest } from '../../../core/models/purchase-order';
import { Supplier } from '../../../core/models/supplier';
import { Warehouse } from '../../../core/models/warehouse';

interface PoDialogProps {
  open: boolean;
  suppliers: Supplier[];
  products: Product[];
  warehouses: Warehouse[];
  currentUserId: number;
  onClose: () => void;
  onSave: (payload: PurchaseOrderRequest) => void;
}

function createItem(): PurchaseOrderItemRequest {
  return { productId: 0, warehouseId: 0, quantityOrdered: 1, unitPrice: 0 };
}

export function PoDialog({ open, suppliers, products, warehouses, currentUserId, onClose, onSave }: PoDialogProps) {
  const [supplierId, setSupplierId] = useState(0);
  const [expectedDate, setExpectedDate] = useState('');
  const [notes, setNotes] = useState('');
  const [items, setItems] = useState<PurchaseOrderItemRequest[]>([createItem()]);
  const [touched, setTouched] = useState(false);

  const itemsInvalid = items.length === 0 || items.some(
    (item) => item.productId < 1 || item.warehouseId < 1 || item.quantityOrdered < 1 || item.unitPrice < 0
  );
  const isInvalid = supplierId < 1 || !expectedDate || itemsInvalid;

  function addItem(): void {
    setItems((current) => [...current, createItem()]);
  }

  function removeItem(index: number): void {
    setItems((current) => current.filter((_, i) => i !== index));
  }

  function updateItem(index: number, patch: Partial<PurchaseOrderItemRequest>): void {
    setItems((current) => current.map((item, i) => (i === index ? { ...item, ...patch } : item)));
  }

  function save(): void {
    if (isInvalid) {
      setTouched(true);
      return;
    }
    onSave({ supplierId, expectedDate, notes, orderedBy: currentUserId, items });
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>Create Purchase Order</DialogTitle>
      <DialogContent>
        <div className="dialog-form">
          <TextField
            variant="outlined"
            select
            label="Supplier"
            value={supplierId}
            onChange={(e) => setSupplierId(Number(e.target.value))}
            error={touched && supplierId < 1}
          >
            {suppliers.map((supplier) => (
              <MenuItem key={supplier.id} value={supplier.id}>{supplier.name}</MenuItem>
            ))}
          </TextField>

          <TextField
            variant="outlined"
            label="Expected Date"
            type="date"
            InputLabelProps={{ shrink: true }}
            value={expectedDate}
            onChange={(e) => setExpectedDate(e.target.value)}
            error={touched && !expectedDate}
          />

          <TextField
            className="full-span"
            variant="outlined"
            label="Notes"
            multiline
            rows={3}
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
          />

          <div className="items-section full-span">
            <div className="items-header">
              <h3>Items</h3>
              <Button variant="outlined" startIcon={<AddIcon />} type="button" onClick={addItem}>
                Add Item
              </Button>
            </div>

            {items.map((item, index) => (
              <div key={index} className="item-row">
                <TextField
                  variant="outlined"
                  select
                  label="Product"
                  value={item.productId}
                  onChange={(e) => updateItem(index, { productId: Number(e.target.value) })}
                  error={touched && item.productId < 1}
                >
                  {products.map((product) => (
                    <MenuItem key={product.id} value={product.id}>{product.name}</MenuItem>
                  ))}
                </TextField>
                <TextField
                  variant="outlined"
                  select
                  label="Warehouse"
                  value={item.warehouseId}
                  onChange={(e) => updateItem(index, { warehouseId: Number(e.target.value) })}
                  error={touched && item.warehouseId < 1}
                >
                  {warehouses.map((warehouse) => (
                    <MenuItem key={warehouse.id} value={warehouse.id}>{warehouse.name}</MenuItem>
                  ))}
                </TextField>
                <TextField
                  variant="outlined"
                  label="Quantity"
                  type="number"
                  inputProps={{ min: 1 }}
                  value={item.quantityOrdered}
                  onChange={(e) => updateItem(index, { quantityOrdered: Number(e.target.value) })}
                  error={touched && item.quantityOrdered < 1}
                />
                <TextField
                  variant="outlined"
                  label="Unit Price (₹)"
                  type="text"
                  value={item.unitPrice ? new Intl.NumberFormat('en-IN').format(item.unitPrice) : ''}
                  onChange={(e) => {
                     const parsed = Number(e.target.value.replace(/,/g, '').replace(/[^0-9]/g, ''));
                     updateItem(index, { unitPrice: parsed });
                  }}
                  error={touched && item.unitPrice < 0}
                />
                <IconButton color="warning" type="button" onClick={() => removeItem(index)} disabled={items.length === 1}>
                  <DeleteIcon />
                </IconButton>
              </div>
            ))}
          </div>
        </div>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" color="primary" disabled={isInvalid} onClick={save}>
          Create PO
        </Button>
      </DialogActions>
    </Dialog>
  );
}
