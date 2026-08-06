import { http } from '../http';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/user';

function normalizeUser(user: User & { active?: boolean }): User {
  return { ...user, isActive: user.isActive ?? user.active ?? false };
}

export const authService = {
  async login(request: LoginRequest): Promise<AuthResponse> {
    const { data } = await http.post<AuthResponse>('/api/v1/auth/login', request);
    localStorage.setItem('token', data.token);
    localStorage.setItem('role', data.role);
    localStorage.setItem('fullName', data.fullName);
    localStorage.setItem('email', data.email);
    if (data.assignedWarehouseId) {
      localStorage.setItem('assignedWarehouseId', data.assignedWarehouseId.toString());
    }
    return data;
  },

  async register(request: RegisterRequest): Promise<User> {
    const { data } = await http.post<User>('/api/v1/auth/register', request);
    return normalizeUser(data);
  },

  async logout(): Promise<void> {
    await http.post<void>('/api/v1/auth/logout', {});
    authService.clearStorage();
  },

  clearStorage(): void {
    ['token', 'role', 'fullName', 'email', 'assignedWarehouseId'].forEach((key) => localStorage.removeItem(key));
  },

  getToken(): string | null { return localStorage.getItem('token'); },
  getRole(): string | null { return localStorage.getItem('role'); },
  getFullName(): string | null { return localStorage.getItem('fullName'); },
  getAssignedWarehouseId(): number | null { 
    const val = localStorage.getItem('assignedWarehouseId'); 
    return val ? Number(val) : null; 
  },
  isLoggedIn(): boolean { return !!authService.getToken(); },
  isAdmin(): boolean { return authService.getRole() === 'ADMIN'; },
  isAdminOrManager(): boolean { return ['ADMIN', 'MANAGER'].includes(authService.getRole() || ''); },
  hasAnyRole(roles: string[]): boolean { return roles.includes(authService.getRole() || ''); },
  canAccessAlerts(): boolean { return authService.hasAnyRole(['ADMIN', 'MANAGER', 'STAFF', 'AUDITOR']); },
  canAccessReports(): boolean { return authService.hasAnyRole(['ADMIN', 'MANAGER', 'AUDITOR']); },
  canManageUsers(): boolean { return authService.isAdmin(); },
  canWriteInventory(): boolean { return authService.hasAnyRole(['ADMIN', 'MANAGER']); },
  canCreateMovement(): boolean { return authService.hasAnyRole(['ADMIN', 'MANAGER', 'STAFF']); },

  async getAllUsers(): Promise<User[]> {
    const { data } = await http.get<User[]>('/api/v1/auth/users');
    return data.map((user) => normalizeUser(user));
  },

  async deactivateUser(id: number): Promise<void> {
    await http.put<void>(`/api/v1/auth/users/${id}/deactivate`, {});
  },

  async reactivateUser(id: number): Promise<void> {
    await http.put<void>(`/api/v1/auth/users/${id}/reactivate`, {});
  }
};
