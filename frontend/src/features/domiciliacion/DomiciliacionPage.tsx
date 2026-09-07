import { useState } from "react";
import { useForm } from "react-hook-form";
import { Zap, PlusCircle } from "lucide-react";
import { useActivarDomiciliacion, useActualizarDomiciliacion, useDomiciliaciones } from "../../hooks/useDomiciliaciones";
import { useCuentas } from "../../hooks/useCuentas";
import { useServicios } from "../../hooks/useServicios";
import { Section } from "../../components/Card";
import { Button } from "../../components/Button";
import { SelectField } from "../../components/Field";
import { Modal } from "../../components/Modal";
import { EstadoBadge } from "../../components/Badge";
import { EmptyState } from "../../components/EmptyState";
import { useToast, apiErrorMessage } from "../../components/Toast";
import type { ActivarDomiciliacionRequest, DomiciliacionResponse } from "../../types/api";

export function DomiciliacionPage() {
  const { data: domiciliaciones = [], isLoading } = useDomiciliaciones();
  const [modalAbierto, setModalAbierto] = useState(false);
  const actualizar = useActualizarDomiciliacion();
  const notify = useToast();

  const alternar = async (d: DomiciliacionResponse) => {
    try {
      await actualizar.mutateAsync({ id: d.id, data: { estado: d.estado === "ACTIVA" ? "INACTIVA" : "ACTIVA" } });
      notify(d.estado === "ACTIVA" ? "Domiciliación desactivada" : "Domiciliación activada", "success");
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos actualizar la domiciliación"), "error");
    }
  };

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "var(--space-6) 0 var(--space-4)" }}>
        <h1 style={{ fontSize: "1.4rem" }}>Domiciliación automática</h1>
        <Button variant="primary" size="sm" onClick={() => setModalAbierto(true)}>
          <PlusCircle size={16} /> Activar
        </Button>
      </div>
      <p style={{ color: "var(--ink-soft)", marginBottom: "var(--space-5)", fontSize: "0.9rem" }}>
        Tus facturas se pagarán automáticamente desde la cuenta elegida el día del vencimiento.
      </p>

      {!isLoading && domiciliaciones.length === 0 ? (
        <EmptyState icon={Zap} title="Sin domiciliaciones activas" description="Activa el pago automático para no volver a preocuparte por una fecha de vencimiento." />
      ) : (
        <Section title="Tus domiciliaciones">
          <div style={{ display: "flex", flexDirection: "column", gap: "var(--space-3)" }}>
            {domiciliaciones.map((d) => (
              <div key={d.id} style={{ display: "flex", alignItems: "center", gap: "var(--space-3)", background: "var(--surface)", border: "1px solid var(--line-soft)", borderRadius: "var(--radius-md)", padding: "var(--space-4)", boxShadow: "var(--shadow-card)" }}>
                <Zap size={18} color="var(--teal)" />
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 600 }}>Ref. {d.numeroReferencia}</div>
                  <div style={{ fontSize: "0.82rem", color: "var(--ink-soft)" }}>Desde {d.cuentaEnmascarada}</div>
                </div>
                <EstadoBadge estado={d.estado} />
                <Button size="sm" variant="secondary" onClick={() => alternar(d)} loading={actualizar.isPending}>
                  {d.estado === "ACTIVA" ? "Desactivar" : "Activar"}
                </Button>
              </div>
            ))}
          </div>
        </Section>
      )}

      {modalAbierto && <ActivarDomiciliacionModal onClose={() => setModalAbierto(false)} />}
    </div>
  );
}

function ActivarDomiciliacionModal({ onClose }: { onClose: () => void }) {
  const { data: servicios = [] } = useServicios();
  const { data: cuentas = [] } = useCuentas();
  const activar = useActivarDomiciliacion();
  const notify = useToast();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ActivarDomiciliacionRequest>();

  const onSubmit = async (data: ActivarDomiciliacionRequest) => {
    try {
      await activar.mutateAsync(data);
      notify("Domiciliación activada", "success");
      onClose();
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos activar la domiciliación"), "error");
    }
  };

  return (
    <Modal title="Activar domiciliación" onClose={onClose}>
      <form style={{ display: "flex", flexDirection: "column", gap: "var(--space-4)" }} onSubmit={handleSubmit(onSubmit)} noValidate>
        <SelectField label="Servicio" error={errors.servicioInscritoId?.message} {...register("servicioInscritoId", { required: "Elige un servicio" })}>
          <option value="">Elige un servicio</option>
          {servicios.map((s) => (
            <option key={s.id} value={s.id}>
              {s.alias || s.empresaRazonSocial}
            </option>
          ))}
        </SelectField>
        <SelectField label="Cuenta de pago" error={errors.cuentaId?.message} {...register("cuentaId", { required: "Elige una cuenta" })}>
          <option value="">Elige una cuenta</option>
          {cuentas.map((c) => (
            <option key={c.id} value={c.id}>
              {c.alias || c.numeroCuentaEnmascarado}
            </option>
          ))}
        </SelectField>
        <Button type="submit" variant="primary" full loading={isSubmitting}>
          Activar
        </Button>
      </form>
    </Modal>
  );
}
