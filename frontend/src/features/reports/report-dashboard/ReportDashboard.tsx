import { useEffect, useState } from 'react';
import {
  Card,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip
} from '@mui/material';
import { LowStockResponse, StockValueResponse } from '../../../core/models/report';
import { reportService } from '../../../core/services/report';
import { useSnackbar, getErrorMessage } from '../../../shared/hooks/useSnackbar';
import { SnackbarHost } from '../../../shared/components/SnackbarHost';
import { formatCurrencyINR } from '../../../shared/utils/format';
import './ReportDashboard.css';

export function ReportDashboard() {
  const [loading, setLoading] = useState(true);
  const [stockValue, setStockValue] = useState<StockValueResponse | null>(null);
  const [lowStock, setLowStock] = useState<LowStockResponse | null>(null);
  const { snackbar, showMessage, close } = useSnackbar();

  useEffect(() => {
    setLoading(true);
    Promise.all([reportService.getStockValue(), reportService.getLowStock()])
      .then(([stockValueData, lowStockData]) => {
        setStockValue(stockValueData);
        setLowStock(lowStockData);
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to load reports')))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Reports</h1>
          <p>Stock valuation and low stock exposure across the business.</p>
        </div>
      </div>

      {loading && (
        <div className="loading-state">
          <CircularProgress size={44} />
        </div>
      )}

      {!loading && stockValue && lowStock && (
        <div className="report-grid">
          <div className="summary-grid">
            <Card className="summary-card">
              <span>Total Stock Value</span>
              <strong>{formatCurrencyINR(stockValue.totalStockValue)}</strong>
            </Card>
            <Card className="summary-card">
              <span>Total Products</span>
              <strong>{stockValue.totalProducts}</strong>
            </Card>
            <Card className="summary-card">
              <span>Total Units</span>
              <strong>{stockValue.totalUnits}</strong>
            </Card>
            <Card className="summary-card danger">
              <span>Low Stock Products</span>
              <strong>{lowStock.totalLowStockProducts}</strong>
            </Card>
          </div>

          <Card>
            <div className="table-title">Stock Value Breakdown</div>
            <Table className="mat-card-table">
              <TableHead>
                <TableRow>
                  <TableCell>Product</TableCell>
                  <TableCell><Tooltip title="SKU = Stock Keeping Unit"><span>SKU</span></Tooltip></TableCell>
                  <TableCell>Quantity</TableCell>
                  <TableCell>Unit Price</TableCell>
                  <TableCell>Total Value</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {stockValue.breakdown.map((item) => (
                  <TableRow key={item.productId}>
                    <TableCell>{item.productName}</TableCell>
                    <TableCell>{item.sku}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>{formatCurrencyINR(item.unitPrice)}</TableCell>
                    <TableCell>{formatCurrencyINR(item.totalValue)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Card>

          <Card>
            <div className="table-title">Low Stock</div>
            <Table className="mat-card-table">
              <TableHead>
                <TableRow>
                  <TableCell>Product</TableCell>
                  <TableCell><Tooltip title="SKU = Stock Keeping Unit"><span>SKU</span></Tooltip></TableCell>
                  <TableCell>Current</TableCell>
                  <TableCell>Reorder Level</TableCell>
                  <TableCell>Shortage</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lowStock.items.map((item) => (
                  <TableRow key={item.productId}>
                    <TableCell>{item.productName}</TableCell>
                    <TableCell>{item.sku}</TableCell>
                    <TableCell>{item.currentQuantity}</TableCell>
                    <TableCell>{item.reorderLevel}</TableCell>
                    <TableCell>{item.shortage}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Card>
        </div>
      )}

      <SnackbarHost snackbar={snackbar} onClose={close} />
    </div>
  );
}
