import { get, post } from './client';
import type { SeckillActivity, SeckillResult } from '../types/api';

export function listSeckillActivities(): Promise<SeckillActivity[]> {
  return get<SeckillActivity[]>('/seckill/activities');
}

export function getSeckillActivity(id: number): Promise<SeckillActivity> {
  return get<SeckillActivity>(`/seckill/activities/${id}`);
}

export function purchaseSeckill(id: number, requestId: string, quantity = 1): Promise<SeckillResult> {
  return post<SeckillResult>(`/seckill/activities/${id}/orders`, { requestId, quantity });
}

export function getSeckillResult(requestId: string): Promise<SeckillResult> {
  return get<SeckillResult>(`/seckill/requests/${requestId}`);
}
