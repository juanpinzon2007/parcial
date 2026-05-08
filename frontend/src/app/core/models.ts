export type Role = 'ADMIN' | 'USER';
export type OrderStatus = 'PENDING' | 'PAID' | 'DELIVERED' | 'CANCELLED';

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface Product {
  id: number;
  name: string;
  brand: string;
  category: string;
  description: string | null;
  price: number;
  stock: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductPayload {
  name: string;
  brand: string;
  category: string;
  description: string | null;
  price: number;
  stock: number;
  active: boolean;
}

export interface InventorySummary {
  totalProducts: number;
  totalUnitsInStock: number;
  lowStockProducts: number;
}

export interface OrderItem {
  id: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: number;
  customerEmail: string;
  customerName: string;
  status: OrderStatus;
  notes: string | null;
  total: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
}

export interface OrderItemPayload {
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface OrderPayload {
  items: OrderItemPayload[];
  notes: string | null;
  status: OrderStatus;
}

export interface RuntimeConfig {
  authApiUrl: string;
  inventoryApiUrl: string;
  salesApiUrl: string;
}
