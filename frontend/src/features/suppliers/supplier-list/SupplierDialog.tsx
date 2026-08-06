import { useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField
} from '@mui/material';
import { Supplier, SupplierRequest } from '../../../core/models/supplier';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface SupplierDialogProps {
  open: boolean;
  supplier: Supplier | null;
  onClose: () => void;
  onSave: (payload: SupplierRequest) => void;
}

export function SupplierDialog({ open, supplier, onClose, onSave }: SupplierDialogProps) {
  const [name, setName] = useState(supplier?.name ?? '');
  const [contactName, setContactName] = useState(supplier?.contactName ?? '');
  const [email, setEmail] = useState(supplier?.email ?? '');
  const [phone, setPhone] = useState(supplier?.phone ?? '');
  const [address, setAddress] = useState(supplier?.address ?? '');
  const [touched, setTouched] = useState(false);

  const isInvalid = !name || !contactName || !email || !EMAIL_PATTERN.test(email) || !phone || !address;

  function save(): void {
    if (isInvalid) {
      setTouched(true);
      return;
    }
    onSave({ name, contactName, email, phone, address });
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{supplier ? 'Edit Supplier' : 'Add Supplier'}</DialogTitle>
      <DialogContent>
        <div className="dialog-form">
          <TextField
            variant="outlined"
            label="Supplier Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            error={touched && !name}
          />
          <TextField
            variant="outlined"
            label="Contact Name"
            value={contactName}
            onChange={(e) => setContactName(e.target.value)}
            error={touched && !contactName}
          />
          <TextField
            variant="outlined"
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            error={touched && (!email || !EMAIL_PATTERN.test(email))}
          />
          <TextField
            variant="outlined"
            label="Phone"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            error={touched && !phone}
          />
          <TextField
            className="full-span"
            variant="outlined"
            label="Address"
            multiline
            rows={3}
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            error={touched && !address}
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
