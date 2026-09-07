import { useState } from "react";
import { ShieldCheck } from "lucide-react";
import { useAuditoria } from "../../hooks/useAuditoria";
import { formatDateTime } from "../../lib/format";
import { Section } from "../../components/Card";
import { InputField, SelectField } from "../../components/Field";
import { EmptyState } from "../../components/EmptyState";

const ACCIONES = [
  "LOGIN_EXITOSO",
  "LOGIN_FALLIDO",
  "PAGO_REGISTRADO",
  "REVERSION_APROBADA",
  "REVERSION_RECHAZADA",
];

export function AuditoriaPage() {
  const [accion, setAccion] = useState("");
  const [usuarioId, setUsuarioId] = useState("");
  const { data: logs = [], isLoading } = useAuditoria({ accion: accion || undefined, usuarioId: usuarioId || undefined });

  return (
    <div>
      <div style={{ padding: "var(--space-6) 0 var(--space-4)" }}>
        <h1 style={{ fontSize: "1.4rem" }}>Auditoría del sistema</h1>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "var(--space-3)", marginBottom: "var(--space-5)" }}>
        <SelectField label="Acción" value={accion} onChange={(e) => setAccion(e.target.value)}>
          <option value="">Todas</option>
          {ACCIONES.map((a) => (
            <option key={a} value={a}>
              {a.replaceAll("_", " ")}
            </option>
          ))}
        </SelectField>
        <InputField label="ID de usuario" placeholder="UUID (opcional)" value={usuarioId} onChange={(e) => setUsuarioId(e.target.value)} />
      </div>

      <Section title={`${logs.length} evento${logs.length === 1 ? "" : "s"}`}>
        {!isLoading && logs.length === 0 ? (
          <EmptyState icon={ShieldCheck} title="Sin eventos" description="No hay registros de auditoría que coincidan con este filtro." />
        ) : (
          <div style={{ overflowX: "auto" }}>
            <div style={{ display: "flex", flexDirection: "column", gap: "var(--space-2)", minWidth: "560px" }}>
              {logs.map((log) => (
                <div key={log.id} style={{ display: "flex", alignItems: "center", gap: "var(--space-4)", background: "var(--surface)", border: "1px solid var(--line-soft)", borderRadius: "var(--radius-sm)", padding: "var(--space-3) var(--space-4)", fontSize: "0.85rem" }}>
                  <span style={{ color: "var(--ink-soft)", flexShrink: 0, width: "150px" }}>{formatDateTime(log.fechaHora)}</span>
                  <span style={{ fontWeight: 600, flexShrink: 0, width: "170px" }}>{log.accion.replaceAll("_", " ")}</span>
                  <span style={{ color: "var(--ink-soft)", flexShrink: 0, width: "110px" }}>{log.direccionIp ?? "—"}</span>
                  <span style={{ flex: 1, color: "var(--ink-soft)" }}>{log.detalle ?? "—"}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </Section>
    </div>
  );
}
