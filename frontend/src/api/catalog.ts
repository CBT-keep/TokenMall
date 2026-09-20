import { get } from './client';
import type { Category, PageResult, ProductDetail, ProductSummary } from '../types/api';

export function listCategories(): Promise<Category[]> {
  return get<Category[]>('/categories');
}

export function listProducts(type?: string, page = 1, size = 20): Promise<PageResult<ProductSummary>> {
  return get<PageResult<ProductSummary>>('/products', { type, page, size });
}

export function getProduct(id: number): Promise<ProductDetail> {
  return get<ProductDetail>(`/products/${id}`);
}
