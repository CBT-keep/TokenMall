import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import * as authApi from '../api/auth';
import { clearSession, getStoredToken, getStoredUser, storeSession } from '../api/client';
import type { UserView } from '../types/api';

interface AuthContextValue {
  token: string | null;
  user: UserView | null;
  login: (username: string, password: string) => Promise<UserView>;
  register: (username: string, password: string, nickname: string) => Promise<UserView>;
  refreshUser: () => Promise<UserView>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getStoredToken());
  const [user, setUser] = useState<UserView | null>(() => getStoredUser<UserView>());

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      user,
      async login(username, password) {
        const response = await authApi.login(username, password);
        storeSession(response.accessToken, response.user);
        setToken(response.accessToken);
        setUser(response.user);
        return response.user;
      },
      async register(username, password, nickname) {
        return authApi.register(username, password, nickname);
      },
      async refreshUser() {
        const current = await authApi.currentUser();
        localStorage.setItem('tokenmall.user', JSON.stringify(current));
        setUser(current);
        return current;
      },
      logout() {
        clearSession();
        setToken(null);
        setUser(null);
      },
    }),
    [token, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
