import { Navigate, Outlet } from 'react-router-dom';
import { authService } from '../services/auth';

interface RoleGuardProps {
  roles?: string[];
}

export function RoleGuard({ roles }: RoleGuardProps) {
  const hasAccess = roles?.length ? authService.hasAnyRole(roles) : authService.isAdmin();
  if (!hasAccess) {
    return <Navigate to="/dashboard" replace />;
  }
  return <Outlet />;
}
