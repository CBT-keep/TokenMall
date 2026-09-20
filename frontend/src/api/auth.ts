import { get, post } from './client';
import type { LoginResponse, UserView } from '../types/api';

export function login(username: string, password: string): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/login', { username, password });
}

export function register(username: string, password: string, nickname: string): Promise<UserView> {
  return post<UserView>('/auth/register', { username, password, nickname });
}

export function currentUser(): Promise<UserView> {
  return get<UserView>('/auth/me');
}
