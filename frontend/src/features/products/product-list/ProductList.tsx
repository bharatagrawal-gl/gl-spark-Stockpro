import { useEffect, useMemo, useState } from 'react';
import {
  Button,
  Card,
  CircularProgress,
  IconButton,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Tooltip
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import SearchIcon from '@mui/icons-material/Search';
import { Product, ProductRequest } from '../../../core/models/product';
import { authService } from '../../../core/services/auth';
import { productService } from '../../../core/services/product';
import { useSnackbar, getErrorMessage } from '../../../shared/hooks/useSnackbar';
import { SnackbarHost } from '../../../shared/components/SnackbarHost';
import { formatCurrencyINR } from '../../../shared/utils/format';
import { ProductDialog } from './ProductDialog';
import './ProductList.css';

type StatusFilter = 'ALL' | 'ACTIVE' | 'INACTIVE';

export function ProductList() {
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [products, setProducts] = useState<Product[]>([]);
  const [canWrite] = useState(authService.canWriteInventory());
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const { snackbar, showMessage, close } = useSnackbar();

  useEffect(() => { loadProducts(); }, []);

  function loadProducts(): void {
    setLoading(true);
    productService.getAll()
      .then(setProducts)
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to load products')))
      .finally(() => setLoading(false));
  }

  const filteredProducts = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    return products.filter((product) => {
      const matchesSearch = !term || [product.name, product.sku, product.barcode].some((value) =>
        value.toLowerCase().includes(term)
      );
      const matchesStatus =
        statusFilter === 'ALL' ||
        (statusFilter === 'ACTIVE' && product.isActive) ||
        (statusFilter === 'INACTIVE' && !product.isActive);

      return matchesSearch && matchesStatus;
    });
  }, [products, searchTerm, statusFilter]);

  function openDialog(product: Product | null = null): void {
    if (!canWrite) return;
    setEditingProduct(product);
    setDialogOpen(true);
  }

  function handleSave(payload: ProductRequest): void {
    const request = editingProduct
      ? productService.update(editingProduct.id, payload)
      : productService.create(payload);

    request
      .then(() => {
        showMessage(`Product ${editingProduct ? 'updated' : 'created'} successfully`);
        setDialogOpen(false);
        loadProducts();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to save product')));
  }

  function toggleStatus(product: Product): void {
    if (!canWrite) return;
    const request = product.isActive
      ? productService.deactivate(product.id)
      : productService.activate(product.id);

    request
      .then(() => {
        showMessage(`Product ${product.isActive ? 'deactivated' : 'reactivated'}`);
        loadProducts();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to update product status')));
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Products</h1>
          <p>Manage catalog items, pricing, and reorder thresholds.</p>
        </div>

        {canWrite && (
          <Button variant="contained" color="primary" startIcon={<AddIcon />} onClick={() => openDialog()}>
            New Product
          </Button>
        )}
      </div>

      <Card>
        <div className="toolbar">
          <TextField
            className="field"
            variant="outlined"
            label="Search"
            placeholder="Name, SKU, or barcode"
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
                  <TableCell>Product</TableCell>
                  <TableCell>
                    <Tooltip title="SKU = Stock Keeping Unit"><span>SKU</span></Tooltip>
                  </TableCell>
                  <TableCell>Price</TableCell>
                  <TableCell>Reorder Level</TableCell>
                  <TableCell>Barcode</TableCell>
                  <TableCell>Status</TableCell>
                  {canWrite && <TableCell />}
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredProducts.map((product) => (
                  <TableRow key={product.id}>
                    <TableCell>
                      <div className="primary-cell">
                        <strong>{product.name}</strong>
                        <span>#{product.id}</span>
                      </div>
                    </TableCell>
                    <TableCell>{product.sku}</TableCell>
                    <TableCell>{formatCurrencyINR(product.price)}</TableCell>
                    <TableCell>{product.reorderLevel}</TableCell>
                    <TableCell>{product.barcode}</TableCell>
                    <TableCell>
                      <span className={`status-chip ${(product.isActive ? 'ACTIVE' : 'INACTIVE').toLowerCase()}`}>
                        {product.isActive ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </TableCell>
                    {canWrite && (
                      <TableCell>
                        <div className="row-actions">
                          <IconButton onClick={() => openDialog(product)}>
                            <EditIcon />
                          </IconButton>
                          <Button variant="outlined" onClick={() => toggleStatus(product)}>
                            {product.isActive ? 'Deactivate' : 'Activate'}
                          </Button>
                        </div>
                      </TableCell>
                    )}
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {filteredProducts.length === 0 && (
              <div className="empty-state">No products match the current filters.</div>
            )}
          </div>
        )}
      </Card>

      {dialogOpen && (
        <ProductDialog
          open={dialogOpen}
          product={editingProduct}
          onClose={() => setDialogOpen(false)}
          onSave={handleSave}
        />
      )}

      <SnackbarHost snackbar={snackbar} onClose={close} />
    </div>
  );
}
