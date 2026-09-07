import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { AppShell } from "../components/AppShell";

export function ProtectedRoute() {
  const { session, loading } = useAuth();

  if (loading) return null;
  if (!session) return <Navigate to="/ingresar" replace />;

  return (
    <AppShell>
      <Outlet />
    </AppShell>
  );
}

export function AdminRoute() {
  const { session } = useAuth();
  if (session?.rol !== "ADMINISTRADOR") return <Navigate to="/panel" replace />;
  return <Outlet />;
}

export function GuestRoute() {
  const { session, loading } = useAuth();
  if (loading) return null;
  if (session) return <Navigate to="/panel" replace />;
  return <Outlet />;
}
