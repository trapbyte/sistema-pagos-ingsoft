import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/apiClient";
import type { AuditLogResponse } from "../types/api";

export function useAuditoria(filtro: { accion?: string; usuarioId?: string }) {
  return useQuery({
    queryKey: ["auditoria", filtro],
    queryFn: () => api.get<AuditLogResponse[]>("/api/auditoria", filtro),
  });
}
