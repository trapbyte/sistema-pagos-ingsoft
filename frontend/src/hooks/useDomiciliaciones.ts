import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/apiClient";
import type { ActivarDomiciliacionRequest, ActualizarDomiciliacionRequest, DomiciliacionResponse } from "../types/api";

const KEY = ["domiciliaciones"];

export function useDomiciliaciones() {
  return useQuery({ queryKey: KEY, queryFn: () => api.get<DomiciliacionResponse[]>("/api/domiciliaciones") });
}

export function useActivarDomiciliacion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: ActivarDomiciliacionRequest) => api.post<DomiciliacionResponse>("/api/domiciliaciones", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useActualizarDomiciliacion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ActualizarDomiciliacionRequest }) =>
      api.patch<DomiciliacionResponse>(`/api/domiciliaciones/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}
