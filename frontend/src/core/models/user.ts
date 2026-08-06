export interface User {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  role: 'ADMIN' | 'MANAGER' | 'STAFF' | 'AUDITOR';
  assignedWarehouseId?: number;
  isActive: boolean;
  createdAt: string;
  lastLoginAt: string;
}
export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  phone: string;
  role: 'ADMIN' | 'MANAGER' | 'STAFF' | 'AUDITOR';
  assignedWarehouseId?: number;
  adminSecret?: string;
}
export interface LoginRequest {
  email: string;
  password: string;
}
export interface AuthResponse {
  token: string;
  email: string;
  role: string;
  fullName: string;
  assignedWarehouseId?: number;
}
