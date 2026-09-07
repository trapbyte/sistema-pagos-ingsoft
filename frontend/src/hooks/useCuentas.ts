import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/apiClient";
import type {
  ActualizarPreferenciaCuentaRequest,
  CuentaResponse,
  PagoResumenResponse,
  VincularCuentaRequest,
} from "../types/api";

const KEY = ["cuentas"];

export function useCuentas() {
  return useQuery({ queryKey: KEY, queryFn: () => api.get<CuentaResponse[]>("/api/cuentas") });
}

export function useMovimientos(cuentaId: string | null) {
  return useQuery({
    queryKey: ["cuentas", cuentaId, "movimientos"],
    queryFn: () => api.get<PagoResumenResponse[]>(`/api/cuentas/${cuentaId}/movimientos`),
    enabled: !!cuentaId,
  });
}

export function useVincularCuenta() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: VincularCuentaRequest) => api.post<CuentaResponse>("/api/cuentas", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useActualizarCuenta() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ cuentaId, data }: { cuentaId: string; data: ActualizarPreferenciaCuentaRequest }) =>
      api.patch<CuentaResponse>(`/api/cuentas/${cuentaId}/preferencias`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useDesvincularCuenta() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (cuentaId: string) => api.delete<void>(`/api/cuentas/${cuentaId}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}
