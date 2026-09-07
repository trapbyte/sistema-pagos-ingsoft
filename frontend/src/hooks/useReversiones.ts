import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/apiClient";
import type { DecidirReversionRequest, ReversionResponse, SolicitarReversionRequest } from "../types/api";

const KEY = ["reversiones"];

export function useReversiones() {
  return useQuery({ queryKey: KEY, queryFn: () => api.get<ReversionResponse[]>("/api/reversiones") });
}

export function useSolicitarReversion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: SolicitarReversionRequest) => api.post<ReversionResponse>("/api/reversiones", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useDecidirReversion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: DecidirReversionRequest }) =>
      api.patch<ReversionResponse>(`/api/reversiones/${id}/decision`, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: KEY });
      qc.invalidateQueries({ queryKey: ["cuentas"] });
    },
  });
}
