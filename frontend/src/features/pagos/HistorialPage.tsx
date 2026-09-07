import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Download, History, RotateCcw } from "lucide-react";
import { useHistorial } from "../../hooks/usePagos";
import { useSolicitarReversion } from "../../hooks/useReversiones";
import { api, downloadBlob } from "../../lib/apiClient";
import { formatMoney, formatDateTime } from "../../lib/format";
import { reversionSchema, type ReversionForm } from "../../lib/schemas";
import { Section } from "../../components/Card";
import { Button } from "../../components/Button";
import { SelectField, InputField, TextareaField } from "../../components/Field";
import { Modal } from "../../components/Modal";
import { EstadoBadge } from "../../components/Badge";
import { EmptyState } from "../../components/EmptyState";
import { useToast, apiErrorMessage } from "../../components/Toast";
import type { EstadoPago, FiltroHistorialPago, PagoResponse, TipoCuenta } from "../../types/api";
import styles from "./HistorialPage.module.css";

export function HistorialPage() {
  const [filtro, setFiltro] = useState<FiltroHistorialPago>({});
  const { data: pagos = [], isLoading } = useHistorial(filtro);
  const notify = useToast();
  const [exportando, setExportando] = useState(false);
  const [pagoAReversar, setPagoAReversar] = useState<PagoResponse | null>(null);

  const set = (patch: Partial<FiltroHistorialPago>) => setFiltro((prev) => ({ ...prev, ...patch }));

  const exportar = async () => {
    setExportando(true);
    try {
      const blob = await api.getBlob("/api/pagos/historial/exportar", filtro as Record<string, string>);
      downloadBlob(blob, "historial-pagos.csv");
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos exportar el historial"), "error");
    } finally {
      setExportando(false);
    }
  };

  return (
    <div>
      <div className={styles.header}>
        <h1 style={{ fontSize: "1.4rem" }}>Historial de pagos</h1>
        <Button variant="secondary" size="sm" onClick={exportar} loading={exportando} disabled={pagos.length === 0}>
          <Download size={15} /> Exportar CSV
        </Button>
      </div>

      <div className={styles.filters}>
        <InputField label="Desde" type="date" value={filtro.fechaDesde ?? ""} onChange={(e) => set({ fechaDesde: e.target.value || undefined })} />
        <InputField label="Hasta" type="date" value={filtro.fechaHasta ?? ""} onChange={(e) => set({ fechaHasta: e.target.value || undefined })} />
        <SelectField label="Tipo de cuenta" value={filtro.tipoCuenta ?? ""} onChange={(e) => set({ tipoCuenta: (e.target.value || undefined) as TipoCuenta | undefined })}>
          <option value="">Todas</option>
          <option value="AHORROS">Ahorros</option>
          <option value="CORRIENTE">Corriente</option>
        </SelectField>
        <SelectField label="Estado" value={filtro.estado ?? ""} onChange={(e) => set({ estado: (e.target.value || undefined) as EstadoPago | undefined })}>
          <option value="">Todos</option>
          <option value="EXITOSO">Exitoso</option>
          <option value="RECHAZADO">Rechazado</option>
          <option value="REVERSADO">Reversado</option>
          <option value="PROCESANDO">Procesando</option>
        </SelectField>
      </div>

      <Section title={`${pagos.length} pago${pagos.length === 1 ? "" : "s"}`}>
        {!isLoading && pagos.length === 0 ? (
          <EmptyState icon={History} title="Sin resultados" description="No hay pagos que coincidan con estos filtros." />
        ) : (
          <div className={styles.list}>
            {pagos.map((pago) => (
              <div key={pago.id} className={styles.row}>
                <div className={styles.rowInfo}>
                  <div className={styles.rowCode}>{pago.codigoComprobante}</div>
                  <div className={styles.rowMeta}>{formatDateTime(pago.fechaHora)} · {pago.tipoProcesamiento === "MANUAL" ? "Manual" : "Domiciliado"}</div>
                </div>
                <EstadoBadge estado={pago.estado} />
                <span className="money" style={{ fontWeight: 700, minWidth: "90px", textAlign: "right" }}>{formatMoney(pago.monto)}</span>
                {pago.estado === "EXITOSO" && (
                  <Button size="sm" variant="ghost" onClick={() => setPagoAReversar(pago)} aria-label="Solicitar reversión">
                    <RotateCcw size={15} />
                  </Button>
                )}
              </div>
            ))}
          </div>
        )}
      </Section>

      {pagoAReversar && <SolicitarReversionModal pago={pagoAReversar} onClose={() => setPagoAReversar(null)} />}
    </div>
  );
}

function SolicitarReversionModal({ pago, onClose }: { pago: PagoResponse; onClose: () => void }) {
  const solicitar = useSolicitarReversion();
  const notify = useToast();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ReversionForm>({ resolver: zodResolver(reversionSchema) });

  const onSubmit = async (data: ReversionForm) => {
    try {
      await solicitar.mutateAsync({ pagoId: pago.id, motivo: data.motivo });
      notify("Solicitud de reversión enviada", "success");
      onClose();
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos solicitar la reversión"), "error");
    }
  };

  return (
    <Modal title={`Reversar ${pago.codigoComprobante}`} onClose={onClose}>
      <form style={{ display: "flex", flexDirection: "column", gap: "var(--space-4)" }} onSubmit={handleSubmit(onSubmit)} noValidate>
        <p style={{ fontSize: "0.85rem", color: "var(--ink-soft)" }}>
          Puedes solicitar la reversión de un pago exitoso dentro de las 24 horas siguientes. Un administrador revisará tu solicitud.
        </p>
        <TextareaField label="Motivo" placeholder="Cuéntanos qué pasó" error={errors.motivo?.message} {...register("motivo")} />
        <Button type="submit" variant="primary" full loading={isSubmitting}>
          Enviar solicitud
        </Button>
      </form>
    </Modal>
  );
}
