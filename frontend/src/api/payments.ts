import { get, post } from './client';
import type { Payment } from '../types/api';

export function getPayment(orderNo: string): Promise<Payment> {
  return get<Payment>(`/payments/${orderNo}`);
}

export function mockPayment(orderNo: string, requestId: string): Promise<Payment> {
  return post<Payment>('/payments/mock/success', { orderNo, requestId });
}
