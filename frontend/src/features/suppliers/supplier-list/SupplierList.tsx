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
  TextField
} from '@mui/material';
import AddBusinessIcon from '@mui/icons-material/AddBusiness';
import EditIcon from '@mui/icons-material/Edit';
import SearchIcon from '@mui/icons-material/Search';
import IconButton from '@mui/material/IconButton';
import { Supplier, SupplierRequest } from '../../../core/models/supplier';
import { authService } from '../../../core/services/auth';
import { supplierService } from '../../../core/services/supplier';
import { useSnackbar, getErrorMessage } from '../../../shared/hooks/useSnackbar';
import { SnackbarHost } from '../../../shared/components/SnackbarHost';
import { SupplierDialog } from './SupplierDialog';
import './SupplierList.css';

type StatusFilter = 'ALL' | 'ACTIVE' | 'INACTIVE';

export function SupplierList() {
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [canWrite] = useState(authService.canWriteInventory());
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState<Supplier | null>(null);
  const { snackbar, showMessage, close } = useSnackbar();

  useEffect(() => { loadSuppliers(); }, []);

  function loadSuppliers(): void {
    setLoading(true);
    supplierService.getAll()
      .then(setSuppliers)
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to load suppliers')))
      .finally(() => setLoading(false));
  }

  const filteredSuppliers = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    return suppliers.filter((supplier) => {
      const matchesSearch = !term || [supplier.name, supplier.contactName, supplier.email, supplier.phone]
        .some((value) => value.toLowerCase().includes(term));
      const matchesStatus =
        statusFilter === 'ALL' ||
        (statusFilter === 'ACTIVE' && supplier.isActive) ||
        (statusFilter === 'INACTIVE' && !supplier.isActive);

      return matchesSearch && matchesStatus;
    });
  }, [suppliers, searchTerm, statusFilter]);

  function openDialog(supplier: Supplier | null = null): void {
    if (!canWrite) return;
    setEditingSupplier(supplier);
    setDialogOpen(true);
  }

  function handleSave(payload: SupplierRequest): void {
    const request = editingSupplier
      ? supplierService.update(editingSupplier.id, payload)
      : supplierService.create(payload);

    request
      .then(() => {
        showMessage(`Supplier ${editingSupplier ? 'updated' : 'created'} successfully`);
        setDialogOpen(false);
        loadSuppliers();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to save supplier')));
  }

  function toggleStatus(supplier: Supplier): void {
    if (!canWrite) return;
    const request = supplier.isActive
      ? supplierService.deactivate(supplier.id)
      : supplierService.activate(supplier.id);

    request
      .then(() => {
        showMessage(`Supplier ${supplier.isActive ? 'deactivated' : 'reactivated'}`);
        loadSuppliers();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to update supplier status')));
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Suppliers</h1>
          <p>Keep vendor details and availability up to date.</p>
        </div>

        {canWrite && (
          <Button variant="contained" color="primary" startIcon={<AddBusinessIcon />} onClick={() => openDialog()}>
            New Supplier
          </Button>
        )}
      </div>

      <Card>
        <div className="toolbar">
          <TextField
            className="field"
            variant="outlined"
            label="Search"
            placeholder="Supplier, contact, email, or phone"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{ endAdornment: <SearchIcon /> }}
          />

          <TextField
            className="field"
            variant="outlined"
            select
            label="Status"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
          >
            <MenuItem value="ALL">All</MenuItem>
            <MenuItem value="ACTIVE">Active</MenuItem>
            <MenuItem value="INACTIVE">Inactive</MenuItem>
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
                  <TableCell>Supplier</TableCell>
                  <TableCell>Contact</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Phone</TableCell>
                  <TableCell>Status</TableCell>
                  {canWrite && <TableCell />}
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredSuppliers.map((supplier) => (
                  <TableRow key={supplier.id}>
                    <TableCell>
                      <div className="primary-cell">
                        <strong>{supplier.name}</strong>
                        <span>{supplier.address}</span>
                      </div>
                    </TableCell>
                    <TableCell>{supplier.contactName}</TableCell>
                    <TableCell>{supplier.email}</TableCell>
                    <TableCell>{supplier.phone}</TableCell>
                    <TableCell>
                      <span className={`status-chip ${(supplier.isActive ? 'ACTIVE' : 'INACTIVE').toLowerCase()}`}>
                        {supplier.isActive ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </TableCell>
                    {canWrite && (
                      <TableCell>
                        <div className="row-actions">
                          <IconButton onClick={() => openDialog(supplier)}>
                            <EditIcon />
                          </IconButton>
                          <Button variant="outlined" onClick={() => toggleStatus(supplier)}>
                            {supplier.isActive ? 'Deactivate' : 'Activate'}
                          </Button>
                        </div>
                      </TableCell>
                    )}
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {filteredSuppliers.length === 0 && (
              <div className="empty-state">No suppliers match the current filters.</div>
            )}
          </div>
        )}
      </Card>

      {dialogOpen && (
        <SupplierDialog
          open={dialogOpen}
          supplier={editingSupplier}
          onClose={() => setDialogOpen(false)}
          onSave={handleSave}
        />
      )}

      <SnackbarHost snackbar={snackbar} onClose={close} />
    </div>
  );
}
