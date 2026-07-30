import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import apiClient, { TOKEN_KEY } from '../lib/apiClient';

interface AuthState {
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));

  const login = useCallback(async (email: string, password: string) => {
    const res = await apiClient.post('/api/auth/login', { email, password });
    const jwt: string = res.data.token;
    localStorage.setItem(TOKEN_KEY, jwt);
    setToken(jwt);
  }, []);

  const register = useCallback(async (email: string, password: string) => {
    const res = await apiClient.post('/api/auth/register', { email, password });
    const jwt: string = res.data.token;
    localStorage.setItem(TOKEN_KEY, jwt);
    setToken(jwt);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
  }, []);

  return (
    <AuthContext.Provider value={{ token, isAuthenticated: !!token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
