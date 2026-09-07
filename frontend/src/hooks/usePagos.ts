import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/apiClient";
import type {
  ComprobanteResponse,
  FiltroHistorialPago,
  PagoResponse,
  RegistrarPagoLoteRequest,
  RegistrarPagoRequest,
} from "../types/api";

export function usePago(pagoId: string | undefined) {
  return useQuery({
    queryKey: ["pagos", pagoId],
    queryFn: () => api.get<PagoResponse>(`/api/pagos/${pagoId}`),
    enabled: !!pagoId,
  });
}

export function useComprobante(pagoId: string | undefined) {
  return useQuery({
    queryKey: ["pagos", pagoId, "comprobante"],
    queryFn: () => api.get<ComprobanteResponse>(`/api/pagos/${pagoId}/comprobante`),
    enabled: !!pagoId,
  });
}

export function useHistorial(filtro: FiltroHistorialPago) {
  return useQuery({
    queryKey: ["historial", filtro],
    queryFn: () => api.get<PagoResponse[]>("/api/pagos/historial", filtro as Record<string, string>),
  });
}

function invalidateAfterPago(qc: ReturnType<typeof useQueryClient>) {
  qc.invalidateQueries({ queryKey: ["cuentas"] });
  qc.invalidateQueries({ queryKey: ["servicios"] });
  qc.invalidateQueries({ queryKey: ["historial"] });
}

export function useRegistrarPago() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: RegistrarPagoRequest) => api.post<PagoResponse>("/api/pagos", data),
    onSuccess: () => invalidateAfterPago(qc),
  });
}

export function useRegistrarPagoLote() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: RegistrarPagoLoteRequest) => api.post<PagoResponse[]>("/api/pagos/lote", data),
    onSuccess: () => invalidateAfterPago(qc),
  });
}
