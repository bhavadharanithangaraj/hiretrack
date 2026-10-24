"use client";

import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { apiFetch, clearSession, getStoredUser, getToken, saveSession } from "@/lib/api";
import type { AuthResponse, CurrentUser } from "@/lib/types";

interface AuthContextValue {
  user: CurrentUser | null;
  ready: boolean; // false until we've read localStorage, so pages don't redirect too early
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [ready, setReady] = useState(false);

  // Restore the session after a page refresh
  useEffect(() => {
    if (getToken()) setUser(getStoredUser<CurrentUser>());
    setReady(true);
  }, []);

  const handleAuth = useCallback((response: AuthResponse) => {
    const { token, ...currentUser } = response;
    saveSession(token, currentUser);
    setUser(currentUser);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await apiFetch<AuthResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      handleAuth(response);
    },
    [handleAuth],
  );

  const register = useCallback(
    async (name: string, email: string, password: string) => {
      const response = await apiFetch<AuthResponse>("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({ name, email, password }),
      });
      handleAuth(response);
    },
    [handleAuth],
  );

  const logout = useCallback(() => {
    clearSession();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, ready, login, register, logout }}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside <AuthProvider>");
  return context;
}
