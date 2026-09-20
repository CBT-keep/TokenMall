import { get, post } from './client';
import type { OrderDetail, OrderSummary, PageResult } from '../types/api';

export function createOrderFromCart(cartItemIds: number[], requestId: string): Promise<OrderDetail> {
  return post<OrderDetail>('/orders', { cartItemIds, requestId });
}

export function createDirectOrder(skuId: number, quantity: number, requestId: string): Promise<OrderDetail> {
  return post<OrderDetail>('/orders/direct', { skuId, quantity, requestId });
}

export function listOrders(status?: string, page = 1, size = 10): Promise<PageResult<OrderSummary>> {
  return get<PageResult<OrderSummary>>('/orders', { status, page, size });
}

export function getOrder(orderNo: string): Promise<OrderDetail> {
  return get<OrderDetail>(`/orders/${orderNo}`);
}

export function cancelOrder(orderNo: string): Promise<void> {
  return post<void>(`/orders/${orderNo}/cancel`);
}
