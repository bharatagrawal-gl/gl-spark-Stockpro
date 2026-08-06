import { useEffect, useState } from 'react';
import { Card, CardContent, CircularProgress, Tooltip } from '@mui/material';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import NotificationsIcon from '@mui/icons-material/Notifications';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import { productService } from '../../core/services/product';
import { warehouseService } from '../../core/services/warehouse';
import { alertService } from '../../core/services/alert';
import { purchaseOrderService } from '../../core/services/purchase-order';
import { authService } from '../../core/services/auth';
import { Alert } from '../../core/models/alert';
import { PurchaseOrder } from '../../core/models/purchase-order';
import './Dashboard.css';

export function Dashboard() {
  const [loading, setLoading] = useState(true);
  const [fullName] = useState(authService.getFullName() || '');
  const [totalProducts, setTotalProducts] = useState(0);
  const [activeProducts, setActiveProducts] = useState(0);
  const [totalWarehouses, setTotalWarehouses] = useState(0);
  const [unreadAlerts, setUnreadAlerts] = useState(0);
  const [pendingPOs, setPendingPOs] = useState(0);
  const [recentAlerts, setRecentAlerts] = useState<Alert[]>([]);
  const [draftPOs, setDraftPOs] = useState<PurchaseOrder[]>([]);

  useEffect(() => {
    const canViewAlerts = authService.canAccessAlerts();

    Promise.all([
      productService.getAll().catch(() => []),
      warehouseService.getActive().catch(() => []),
      canViewAlerts ? alertService.getAll().catch(() => []) : Promise.resolve([] as Alert[]),
      canViewAlerts ? alertService.getUnread().catch(() => []) : Promise.resolve([] as Alert[]),
      purchaseOrderService.getAll().catch(() => [])
    ]).then(([products, warehouses, alerts, unread, pos]) => {
      setTotalProducts(products.length);
      setActiveProducts(products.filter((p) => p.isActive).length);
      setTotalWarehouses(warehouses.length);
      setUnreadAlerts(unread.length);
      setPendingPOs(pos.filter((p) => p.status === 'DRAFT').length);
      setRecentAlerts(alerts.slice(0, 5));
      setDraftPOs(pos.filter((p) => p.status === 'DRAFT').slice(0, 5));
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p>Welcome back, {fullName}. Here is your inventory overview.</p>
        </div>
      </div>

      {loading && (
        <div className="loading-center">
          <CircularProgress size={48} />
        </div>
      )}

      {!loading && (
        <div>
          <div className="kpi-grid">
            <Card className="kpi-card">
              <CardContent>
                <div className="kpi-content">
                  <div>
                    <p className="kpi-label">Total Products</p>
                    <p className="kpi-value">{totalProducts}</p>
                    <p className="kpi-sub green">{activeProducts} active</p>
                  </div>
                  <Inventory2Icon className="kpi-icon blue" />
                </div>
              </CardContent>
            </Card>

            <Card className="kpi-card">
              <CardContent>
                <div className="kpi-content">
                  <div>
                    <p className="kpi-label">Warehouses</p>
                    <p className="kpi-value">{totalWarehouses}</p>
                    <p className="kpi-sub">Active locations</p>
                  </div>
                  <WarehouseIcon className="kpi-icon purple" />
                </div>
              </CardContent>
            </Card>

            <Card className="kpi-card">
              <CardContent>
                <div className="kpi-content">
                  <div>
                    <p className="kpi-label">Unread Alerts</p>
                    <p className={`kpi-value${unreadAlerts > 0 ? ' red' : ''}`}>{unreadAlerts}</p>
                    <p className="kpi-sub">Require attention</p>
                  </div>
                  <NotificationsIcon className="kpi-icon red" />
                </div>
              </CardContent>
            </Card>

            <Card className="kpi-card">
              <CardContent>
                <div className="kpi-content">
                  <div>
                    <Tooltip title="POs = Purchase Orders">
                      <p className="kpi-label">Pending POs</p>
                    </Tooltip>
                    <p className="kpi-value amber">{pendingPOs}</p>
                    <p className="kpi-sub">Awaiting approval</p>
                  </div>
                  <ReceiptLongIcon className="kpi-icon amber" />
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="panels-grid">
            <Card>
              <CardContent>
                <h3>Recent Alerts</h3>
                {recentAlerts.length === 0 && <p className="empty-msg">No alerts</p>}
                {recentAlerts.map((alert) => (
                  <div key={alert.id} className="alert-item">
                    <span className={`status-chip ${alert.alertType.toLowerCase()}`}>{alert.alertType}</span>
                    <p className="alert-msg">{alert.message}</p>
                    {!alert.isRead && <span className="unread-dot" />}
                  </div>
                ))}
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <h3>Draft Purchase Orders</h3>
                {draftPOs.length === 0 && <p className="empty-msg">No draft orders</p>}
                {draftPOs.map((po) => (
                  <div key={po.id} className="po-item">
                    <div>
                      <Tooltip title="PO = Purchase Order">
                        <p className="po-id">PO #{po.id}</p>
                      </Tooltip>
                      <p className="po-supplier">Supplier {po.supplierId}</p>
                    </div>
                    <span className="status-chip draft">{po.status}</span>
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
}
