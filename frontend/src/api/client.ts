import axios from 'axios';
import type { ApiResponse } from '../types/api';

const TOKEN_KEY = 'tokenmall.accessToken';
const USER_KEY = 'tokenmall.user';
export const AUTH_EXPIRED_EVENT = 'tokenmall:auth-expired';

export const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearSession();
      window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
    }
    return Promise.reject(error);
  },
);

export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const response = await client.get<ApiResponse<T>>(url, { params });
  return response.data.data;
}

export async function post<T>(url: string, data?: unknown): Promise<T> {
  const response = await client.post<ApiResponse<T>>(url, data);
  return response.data.data;
}

export async function put<T>(url: string, data?: unknown): Promise<T> {
  const response = await client.put<ApiResponse<T>>(url, data);
  return response.data.data;
}

export async function del<T>(url: string): Promise<T> {
  const response = await client.delete<ApiResponse<T>>(url);
  return response.data.data;
}

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function storeSession(token: string, user: unknown): void {
  localStorage.setItem(TOKEN_KEY, token);
  storeUser(user);
}

export function storeUser(user: unknown): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function getStoredUser<T>(): T | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as T;
  } catch {
    clearSession();
    return null;
  }
}

export function errorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? error.message;
  }
  return error instanceof Error ? error.message : '请求失败';
}
