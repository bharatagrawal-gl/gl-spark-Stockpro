import { Navigate, Outlet } from 'react-router-dom';
import { authService } from '../services/auth';

export function AuthGuard() {
  if (!authService.isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}
