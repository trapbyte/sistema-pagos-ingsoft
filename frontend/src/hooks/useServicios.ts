import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/apiClient";
import type {
  ActualizarAliasServicioRequest,
  EmpresaServicioResponse,
  FacturaResponse,
  InscribirServicioRequest,
  ServicioInscritoResponse,
} from "../types/api";

const KEY = ["servicios"];

export function useEmpresas() {
  return useQuery({ queryKey: ["empresas"], queryFn: () => api.get<EmpresaServicioResponse[]>("/api/empresas") });
}

export function useServicios() {
  return useQuery({ queryKey: KEY, queryFn: () => api.get<ServicioInscritoResponse[]>("/api/servicios") });
}

export function useFactura(servicioId: string) {
  return useQuery({
    queryKey: ["servicios", servicioId, "factura"],
    queryFn: () => api.get<FacturaResponse>(`/api/servicios/${servicioId}/factura`),
    staleTime: 60_000,
  });
}

export function useInscribirServicio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: InscribirServicioRequest) => api.post<ServicioInscritoResponse>("/api/servicios", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useActualizarAliasServicio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ servicioId, data }: { servicioId: string; data: ActualizarAliasServicioRequest }) =>
      api.patch<ServicioInscritoResponse>(`/api/servicios/${servicioId}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useDesinscribirServicio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (servicioId: string) => api.delete<void>(`/api/servicios/${servicioId}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}
