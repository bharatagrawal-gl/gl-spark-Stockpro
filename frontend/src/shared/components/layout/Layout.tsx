import { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import DashboardIcon from '@mui/icons-material/Dashboard';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import BusinessIcon from '@mui/icons-material/Business';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import NotificationsIcon from '@mui/icons-material/Notifications';
import BarChartIcon from '@mui/icons-material/BarChart';
import PeopleIcon from '@mui/icons-material/People';
import LogoutIcon from '@mui/icons-material/Logout';
import { authService } from '../../../core/services/auth';
import { alertService } from '../../../core/services/alert';
import './Layout.css';

export function Layout() {
  const navigate = useNavigate();

  const [fullName] = useState(authService.getFullName() || '');
  const [role] = useState(authService.getRole() || '');
  const [isAdmin] = useState(authService.canManageUsers());
  const [canViewAlerts] = useState(authService.canAccessAlerts());
  const [canViewReports] = useState(authService.canAccessReports());
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (!canViewAlerts) return;

    const unsubscribe = alertService.subscribeUnreadCount(setUnreadCount);
    alertService.refreshUnreadCount().catch(() => {});
    const intervalId = setInterval(() => {
      alertService.refreshUnreadCount().catch(() => {});
    }, 15000);

    return () => {
      unsubscribe();
      clearInterval(intervalId);
    };
  }, [canViewAlerts]);

  const logout = () => {
    authService.logout()
      .then(() => navigate('/login'))
      .catch(() => {
        authService.clearStorage();
        navigate('/login');
      });
  };

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `nav-item${isActive ? ' active-link' : ''}`;

  return (
    <div className="sidenav-container">
      <div className="sidenav">
        <div className="brand">
          <h1>StockPro</h1>
          <p>{fullName}</p>
          <span className={`status-chip ${role.toLowerCase()}`}>{role}</span>
        </div>

        <nav className="nav-menu">
          <NavLink to="/dashboard" className={navLinkClass}>
            <DashboardIcon className="nav-icon" />
            <span>Dashboard</span>
          </NavLink>
          <NavLink to="/products" className={navLinkClass}>
            <Inventory2Icon className="nav-icon" />
            <span>Products</span>
          </NavLink>
          <NavLink to="/suppliers" className={navLinkClass}>
            <BusinessIcon className="nav-icon" />
            <span>Suppliers</span>
          </NavLink>
          <NavLink to="/warehouses" className={navLinkClass}>
            <WarehouseIcon className="nav-icon" />
            <span>Warehouses</span>
          </NavLink>
          <NavLink to="/purchase-orders" className={navLinkClass}>
            <ReceiptLongIcon className="nav-icon" />
            <span>Purchase Orders</span>
          </NavLink>
          <NavLink to="/movements" className={navLinkClass}>
            <SwapHorizIcon className="nav-icon" />
            <span>Movements</span>
          </NavLink>
          {canViewAlerts && (
            <NavLink to="/alerts" className={navLinkClass}>
              <NotificationsIcon className="nav-icon" />
              <span>Alerts</span>
              {unreadCount > 0 && <span className="nav-badge">{unreadCount}</span>}
            </NavLink>
          )}
          {canViewReports && (
            <NavLink to="/reports" className={navLinkClass}>
              <BarChartIcon className="nav-icon" />
              <span>Reports</span>
            </NavLink>
          )}
          {isAdmin && (
            <NavLink to="/users" className={navLinkClass}>
              <PeopleIcon className="nav-icon" />
              <span>Users</span>
            </NavLink>
          )}
        </nav>

        <div className="signout">
          <button className="nav-item signout-btn" onClick={logout} type="button">
            <LogoutIcon className="nav-icon" />
            <span>Sign Out</span>
          </button>
        </div>
      </div>

      <div className="main-content">
        <Outlet />
      </div>
    </div>
  );
}
