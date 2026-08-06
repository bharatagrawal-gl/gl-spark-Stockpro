import { useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField
} from '@mui/material';
import { Warehouse, WarehouseRequest } from '../../../core/models/warehouse';

interface WarehouseDialogProps {
  open: boolean;
  warehouse: Warehouse | null;
  onClose: () => void;
  onSave: (payload: WarehouseRequest) => void;
}

export function WarehouseDialog({ open, warehouse, onClose, onSave }: WarehouseDialogProps) {
  const [name, setName] = useState(warehouse?.name ?? '');
  const [location, setLocation] = useState(warehouse?.location ?? '');
  const [capacity, setCapacity] = useState(warehouse?.capacity ?? 0);
  const [touched, setTouched] = useState(false);

  const isInvalid = !name || !location || capacity < 0;

  function save(): void {
    if (isInvalid) {
      setTouched(true);
      return;
    }
    onSave({ name, location, capacity });
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{warehouse ? 'Edit Warehouse' : 'Add Warehouse'}</DialogTitle>
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
            label="Location"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            error={touched && !location}
          />
          <TextField
            className="full-span"
            variant="outlined"
            label="Capacity"
            type="number"
            inputProps={{ min: 0 }}
            value={capacity}
            onChange={(e) => setCapacity(Number(e.target.value))}
            error={touched && capacity < 0}
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
