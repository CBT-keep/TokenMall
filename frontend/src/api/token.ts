import { get, post } from './client';
import type { TokenAccount, TokenPlan, TokenTransaction } from '../types/api';

export function getTokenAccount(): Promise<TokenAccount> {
  return get<TokenAccount>('/token/account');
}

export function listTokenPlans(): Promise<TokenPlan[]> {
  return get<TokenPlan[]>('/token/plans');
}

export function listTokenTransactions(): Promise<TokenTransaction[]> {
  return get<TokenTransaction[]>('/token/transactions');
}

export function consumeTokens(amount: number, requestId: string, description?: string): Promise<TokenAccount> {
  return post<TokenAccount>('/token/consume', { amount, requestId, description });
}
