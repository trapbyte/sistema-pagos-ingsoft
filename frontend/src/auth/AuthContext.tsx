import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api, getToken, setToken } from "../lib/apiClient";
import type { ClientePerfilResponse, LoginRequest, LoginResponse, RegistroClienteRequest, RolUsuario } from "../types/api";

interface Session {
  clienteId: string;
  rol: RolUsuario;
}

interface AuthState {
  session: Session | null;
  perfil: ClientePerfilResponse | null;
  loading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  registrar: (data: RegistroClienteRequest) => Promise<void>;
  logout: () => void;
  refrescarPerfil: () => Promise<void>;
}

const AuthContext = createContext<AuthState | null>(null);

/** Payload del JWT emitido por JwtService: subject = clienteId, claim "rol". */
function decodeSession(token: string): Session | null {
  try {
    const [, payload] = token.split(".");
    const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    return { clienteId: decoded.sub, rol: decoded.rol };
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => {
    const token = getToken();
    return token ? decodeSession(token) : null;
  });
  const [perfil, setPerfil] = useState<ClientePerfilResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const refrescarPerfil = useCallback(async () => {
    if (!getToken()) {
      setPerfil(null);
      return;
    }
    const data = await api.get<ClientePerfilResponse>("/api/clientes/me");
    setPerfil(data);
  }, []);

  useEffect(() => {
    (async () => {
      if (session) {
        try {
          await refrescarPerfil();
        } catch {
          setToken(null);
          setSession(null);
        }
      }
      setLoading(false);
    })();
  }, [session, refrescarPerfil]);

  useEffect(() => {
    const onUnauthorized = () => {
      setToken(null);
      setSession(null);
      setPerfil(null);
    };
    window.addEventListener("recibo:unauthorized", onUnauthorized);
    return () => window.removeEventListener("recibo:unauthorized", onUnauthorized);
  }, []);

  const login = useCallback(async (data: LoginRequest) => {
    const response = await api.post<LoginResponse>("/api/auth/login", data);
    setToken(response.token);
    setSession(decodeSession(response.token));
  }, []);

  const registrar = useCallback(async (data: RegistroClienteRequest) => {
    await api.post<ClientePerfilResponse>("/api/auth/register", data);
    await login({ email: data.email, password: data.password });
  }, [login]);

  const logout = useCallback(() => {
    setToken(null);
    setSession(null);
    setPerfil(null);
  }, []);

  const value = useMemo(
    () => ({ session, perfil, loading, login, registrar, logout, refrescarPerfil }),
    [session, perfil, loading, login, registrar, logout, refrescarPerfil],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth debe usarse dentro de <AuthProvider>");
  return ctx;
}
