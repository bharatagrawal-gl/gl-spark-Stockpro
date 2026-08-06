import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthGuard } from './core/guards/auth-guard';
import { RoleGuard } from './core/guards/role-guard';
import { Layout } from './shared/components/layout/Layout';
import { Login } from './features/auth/login/Login';
import { Register } from './features/auth/register/Register';
import { Dashboard } from './features/dashboard/Dashboard';
import { ProductList } from './features/products/product-list/ProductList';
import { SupplierList } from './features/suppliers/supplier-list/SupplierList';
import { WarehouseList } from './features/warehouses/warehouse-list/WarehouseList';
import { PoList } from './features/purchase-orders/po-list/PoList';
import { MovementList } from './features/movements/movement-list/MovementList';
import { AlertList } from './features/alerts/alert-list/AlertList';
import { ReportDashboard } from './features/reports/report-dashboard/ReportDashboard';
import { UserList } from './features/users/user-list/UserList';

export function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<AuthGuard />}>
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/products" element={<ProductList />} />
          <Route path="/suppliers" element={<SupplierList />} />
          <Route path="/warehouses" element={<WarehouseList />} />
          <Route path="/purchase-orders" element={<PoList />} />
          <Route path="/movements" element={<MovementList />} />

          <Route element={<RoleGuard roles={['ADMIN', 'MANAGER', 'STAFF', 'AUDITOR']} />}>
            <Route path="/alerts" element={<AlertList />} />
          </Route>

          <Route element={<RoleGuard roles={['ADMIN', 'MANAGER', 'AUDITOR']} />}>
            <Route path="/reports" element={<ReportDashboard />} />
          </Route>

          <Route element={<RoleGuard roles={['ADMIN']} />}>
            <Route path="/users" element={<UserList />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
