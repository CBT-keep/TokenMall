import { del, get, post, put } from './client';
import type {
  Category,
  DashboardData,
  OrderDetail,
  OrderSummary,
  PageResult,
  ProductSummary,
  SeckillActivity,
  Sku,
} from '../types/api';

export function getDashboard(): Promise<DashboardData> {
  return get<DashboardData>('/admin/dashboard');
}

export function listAdminCategories(): Promise<Category[]> {
  return get<Category[]>('/admin/categories');
}

export function createCategory(data: unknown): Promise<Category> {
  return post<Category>('/admin/categories', data);
}

export function updateCategory(id: number, data: unknown): Promise<Category> {
  return put<Category>(`/admin/categories/${id}`, data);
}

export function deleteCategory(id: number): Promise<void> {
  return del<void>(`/admin/categories/${id}`);
}

export function listAdminProducts(): Promise<ProductSummary[]> {
  return get<ProductSummary[]>('/admin/products');
}

export function createProduct(data: unknown): Promise<ProductSummary> {
  return post<ProductSummary>('/admin/products', data);
}

export function updateProduct(id: number, data: unknown): Promise<ProductSummary> {
  return put<ProductSummary>(`/admin/products/${id}`, data);
}

export function deleteProduct(id: number): Promise<void> {
  return del<void>(`/admin/products/${id}`);
}

export function listAdminSkus(): Promise<Sku[]> {
  return get<Sku[]>('/admin/skus');
}

export function createSku(data: unknown): Promise<Sku> {
  return post<Sku>('/admin/skus', data);
}

export function updateSku(id: number, data: unknown): Promise<Sku> {
  return put<Sku>(`/admin/skus/${id}`, data);
}

export function adjustInventory(
  skuId: number,
  data: { totalStock: number; availableStock: number; lockedStock: number },
): Promise<unknown> {
  return put(`/admin/inventory/${skuId}/adjust`, data);
}

export function listAdminOrders(status?: string, page = 1, size = 20): Promise<PageResult<OrderSummary>> {
  return get<PageResult<OrderSummary>>('/admin/orders', { status, page, size });
}

export function getAdminOrder(orderNo: string): Promise<OrderDetail> {
  return get<OrderDetail>(`/admin/orders/${orderNo}`);
}

export function listAdminSeckillActivities(): Promise<SeckillActivity[]> {
  return get<SeckillActivity[]>('/admin/seckill/activities');
}

export function createSeckillActivity(data: unknown): Promise<SeckillActivity> {
  return post<SeckillActivity>('/admin/seckill/activities', data);
}

export function updateSeckillActivity(id: number, data: unknown): Promise<SeckillActivity> {
  return put<SeckillActivity>(`/admin/seckill/activities/${id}`, data);
}

export function deleteSeckillActivity(id: number): Promise<void> {
  return del<void>(`/admin/seckill/activities/${id}`);
}

export function getSeckillRecords(id: number): Promise<unknown[]> {
  return get<unknown[]>(`/admin/seckill/activities/${id}/records`);
}
