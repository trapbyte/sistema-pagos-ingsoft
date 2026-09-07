import { useState } from "react";
import { RotateCcw, Check, X } from "lucide-react";
import { useAuth } from "../../auth/AuthContext";
import { useDecidirReversion, useReversiones } from "../../hooks/useReversiones";
import { formatDateTime } from "../../lib/format";
import { Section } from "../../components/Card";
import { Button } from "../../components/Button";
import { Modal } from "../../components/Modal";
import { TextareaField } from "../../components/Field";
import { EstadoBadge } from "../../components/Badge";
import { EmptyState } from "../../components/EmptyState";
import { useToast, apiErrorMessage } from "../../components/Toast";
import type { ReversionResponse } from "../../types/api";

export function ReversionesPage() {
  const { session } = useAuth();
  const esAdmin = session?.rol === "ADMINISTRADOR";
  const { data: reversiones = [], isLoading } = useReversiones();
  const [enDecision, setEnDecision] = useState<ReversionResponse | null>(null);

  return (
    <div>
      <div style={{ padding: "var(--space-6) 0 var(--space-4)" }}>
        <h1 style={{ fontSize: "1.4rem" }}>{esAdmin ? "Reversiones por revisar" : "Tus reversiones"}</h1>
      </div>

      {!isLoading && reversiones.length === 0 ? (
        <EmptyState icon={RotateCcw} title="No hay solicitudes" description={esAdmin ? "No hay reversiones pendientes de decisión." : "Puedes solicitar la reversión de un pago exitoso desde tu historial."} />
      ) : (
        <Section title={`${reversiones.length} solicitud${reversiones.length === 1 ? "" : "es"}`}>
          <div style={{ display: "flex", flexDirection: "column", gap: "var(--space-3)" }}>
            {reversiones.map((r) => (
              <div key={r.id} style={{ background: "var(--surface)", border: "1px solid var(--line-soft)", borderRadius: "var(--radius-md)", padding: "var(--space-4)", boxShadow: "var(--shadow-card)" }}>
                <div style={{ display: "flex", justifyContent: "space-between", gap: "var(--space-3)" }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>{r.codigoComprobantePago}</div>
                    <div style={{ fontSize: "0.82rem", color: "var(--ink-soft)" }}>Solicitada {formatDateTime(r.fechaSolicitud)}</div>
                  </div>
                  <EstadoBadge estado={r.estado} />
                </div>
                <p style={{ fontSize: "0.88rem", marginTop: "var(--space-3)" }}>{r.motivo}</p>
                {r.respuestaAdministrador && (
                  <p style={{ fontSize: "0.82rem", color: "var(--ink-soft)", marginTop: "var(--space-2)" }}>
                    Respuesta: {r.respuestaAdministrador}
                  </p>
                )}
                {esAdmin && r.estado === "SOLICITADA" && (
                  <div style={{ display: "flex", gap: "var(--space-2)", marginTop: "var(--space-4)" }}>
                    <Button size="sm" variant="primary" onClick={() => setEnDecision(r)}>
                      <Check size={15} /> Decidir
                    </Button>
                  </div>
                )}
              </div>
            ))}
          </div>
        </Section>
      )}

      {enDecision && <DecidirReversionModal reversion={enDecision} onClose={() => setEnDecision(null)} />}
    </div>
  );
}

function DecidirReversionModal({ reversion, onClose }: { reversion: ReversionResponse; onClose: () => void }) {
  const decidir = useDecidirReversion();
  const notify = useToast();
  const [respuesta, setRespuesta] = useState("");

  const enviar = async (aprobar: boolean) => {
    try {
      await decidir.mutateAsync({ id: reversion.id, data: { aprobar, respuesta: respuesta || undefined } });
      notify(aprobar ? "Reversión aprobada" : "Reversión rechazada", "success");
      onClose();
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos registrar la decisión"), "error");
    }
  };

  return (
    <Modal title={`Decidir sobre ${reversion.codigoComprobantePago}`} onClose={onClose}>
      <p style={{ fontSize: "0.88rem", marginBottom: "var(--space-4)" }}>{reversion.motivo}</p>
      <TextareaField label="Respuesta (opcional)" value={respuesta} onChange={(e) => setRespuesta(e.target.value)} placeholder="Explica tu decisión al cliente" />
      <div style={{ display: "flex", gap: "var(--space-2)", marginTop: "var(--space-4)" }}>
        <Button variant="secondary" full onClick={() => enviar(false)} loading={decidir.isPending}>
          <X size={15} /> Rechazar
        </Button>
        <Button variant="primary" full onClick={() => enviar(true)} loading={decidir.isPending}>
          <Check size={15} /> Aprobar
        </Button>
      </div>
    </Modal>
  );
}
