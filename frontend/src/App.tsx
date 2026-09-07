import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import { AdminRoute, GuestRoute, ProtectedRoute } from "./auth/ProtectedRoute";
import { ToastProvider } from "./components/Toast";
import { LoginPage } from "./features/auth/LoginPage";
import { RegisterPage } from "./features/auth/RegisterPage";
import { DashboardPage } from "./features/dashboard/DashboardPage";
import { ServiciosPage } from "./features/servicios/ServiciosPage";
import { PagarPage } from "./features/pagos/PagarPage";
import { HistorialPage } from "./features/pagos/HistorialPage";
import { DomiciliacionPage } from "./features/domiciliacion/DomiciliacionPage";
import { ReversionesPage } from "./features/reversiones/ReversionesPage";
import { AuditoriaPage } from "./features/auditoria/AuditoriaPage";
import { PerfilPage } from "./features/perfil/PerfilPage";

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              <Route element={<GuestRoute />}>
                <Route path="/ingresar" element={<LoginPage />} />
                <Route path="/registrarse" element={<RegisterPage />} />
              </Route>

              <Route element={<ProtectedRoute />}>
                <Route path="/" element={<DashboardPage />} />
                <Route path="/servicios" element={<ServiciosPage />} />
                <Route path="/pagar" element={<PagarPage />} />
                <Route path="/historial" element={<HistorialPage />} />
                <Route path="/domiciliacion" element={<DomiciliacionPage />} />
                <Route path="/reversiones" element={<ReversionesPage />} />
                <Route path="/perfil" element={<PerfilPage />} />
                <Route element={<AdminRoute />}>
                  <Route path="/auditoria" element={<AuditoriaPage />} />
                </Route>
              </Route>

              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </ToastProvider>
    </QueryClientProvider>
  );
}
