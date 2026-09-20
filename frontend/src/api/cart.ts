import { del, get, post, put } from './client';
import type { CartItem } from '../types/api';

export function listCart(): Promise<CartItem[]> {
  return get<CartItem[]>('/cart');
}

export function addCartItem(skuId: number, quantity: number): Promise<CartItem> {
  return post<CartItem>('/cart/items', { skuId, quantity });
}

export function updateCartItem(id: number, quantity: number, selected: boolean): Promise<CartItem> {
  return put<CartItem>(`/cart/items/${id}`, { quantity, selected });
}

export function deleteCartItem(id: number): Promise<void> {
  return del<void>(`/cart/items/${id}`);
}
