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
import { authService } from '../../../core/services/auth';
import { MovementRequest, MovementType } from '../../../core/models/movement';
import { Product } from '../../../core/models/product';
import { Warehouse } from '../../../core/models/warehouse';

interface MovementDialogProps {
  open: boolean;
  products: Product[];
  warehouses: Warehouse[];
  onClose: () => void;
  onSave: (payload: MovementRequest) => void;
}

export function MovementDialog({ open, products, warehouses, onClose, onSave }: MovementDialogProps) {
  const [movementType, setMovementType] = useState<MovementType>('STOCK_OUT');
  const [productId, setProductId] = useState(0);
  const [warehouseId, setWarehouseId] = useState(0);
  const [quantity, setQuantity] = useState(0);
  const [notes, setNotes] = useState('');
  const [touched, setTouched] = useState(false);
  const assignedWarehouseId = authService.getAssignedWarehouseId();
  const isStaff = authService.getRole() === 'STAFF';

  useState(() => {
    if (assignedWarehouseId) {
      setWarehouseId(assignedWarehouseId);
    }
  });

  const isInvalid = !productId || !warehouseId || quantity <= 0;

  function save(): void {
    if (isInvalid) {
      setTouched(true);
      return;
    }
    const performedBy = Number(localStorage.getItem('userId')) || 1;
    onSave({
      movementType,
      productId,
      warehouseId,
      quantity,
      notes,
      referenceType: 'MANUAL',
      referenceId: 0,
      performedBy
    });
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Add Manual Stock Movement</DialogTitle>
      <DialogContent>
        <div className="dialog-form" style={{ gridTemplateColumns: '1fr', display: 'flex', flexDirection: 'column', gap: '16px', paddingTop: '8px' }}>
          <TextField
            select
            variant="outlined"
            label="Type"
            value={movementType}
            onChange={(e) => setMovementType(e.target.value as MovementType)}
          >
            <MenuItem value="STOCK_OUT">Stock Out</MenuItem>
            {!isStaff && <MenuItem value="STOCK_IN">Stock In</MenuItem>}
          </TextField>

          <TextField
            select
            variant="outlined"
            label="Product"
            value={productId}
            onChange={(e) => setProductId(Number(e.target.value))}
            error={touched && !productId}
          >
            {products.map((p) => (
              <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>
            ))}
          </TextField>

          <TextField
            select
            variant="outlined"
            label="Warehouse"
            value={warehouseId}
            onChange={(e) => setWarehouseId(Number(e.target.value))}
            error={touched && !warehouseId}
            disabled={!!assignedWarehouseId}
          >
            {warehouses.map((w) => (
              <MenuItem key={w.id} value={w.id}>{w.name}</MenuItem>
            ))}
          </TextField>

          <TextField
            variant="outlined"
            label="Quantity"
            type="number"
            inputProps={{ min: 1 }}
            value={quantity || ''}
            onChange={(e) => setQuantity(Number(e.target.value))}
            error={touched && quantity <= 0}
          />

          <TextField
            variant="outlined"
            label="Notes / Reference"
            multiline
            rows={2}
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
          />
        </div>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" color="primary" disabled={isInvalid} onClick={save}>
          Confirm
        </Button>
      </DialogActions>
    </Dialog>
  );
}
