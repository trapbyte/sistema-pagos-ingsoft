import { useMemo, useState } from "react";
import { useQueries } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { CheckCircle2, Download, ReceiptText, Wallet } from "lucide-react";
import { useCuentas } from "../../hooks/useCuentas";
import { useEmpresas, useServicios } from "../../hooks/useServicios";
import { useRegistrarPago, useRegistrarPagoLote } from "../../hooks/usePagos";
import { api, downloadBlob } from "../../lib/apiClient";
import { formatMoney, dueLabel } from "../../lib/format";
import { categoriaVisual } from "../../lib/serviceCategory";
import { Section } from "../../components/Card";
import { Button } from "../../components/Button";
import { EmptyState } from "../../components/EmptyState";
import { useToast, apiErrorMessage } from "../../components/Toast";
import type { FacturaResponse, PagoResponse, ServicioInscritoResponse } from "../../types/api";
import styles from "./PagarPage.module.css";

export function PagarPage() {
  const [params] = useSearchParams();
  const servicioPreseleccionado = params.get("servicioId");
  const notify = useToast();

  const { data: cuentas = [] } = useCuentas();
  const { data: servicios = [] } = useServicios();
  const { data: empresas = [] } = useEmpresas();
  const categoriaPorEmpresa = new Map(empresas.map((e) => [e.id, e.categoria]));

  const facturaQueries = useQueries({
    queries: servicios.map((s) => ({
      queryKey: ["servicios", s.id, "factura"],
      queryFn: () => api.get<FacturaResponse>(`/api/servicios/${s.id}/factura`),
      staleTime: 30_000,
    })),
  });

  const pendientes = useMemo(
    () =>
      servicios
        .map((servicio, i) => ({ servicio, factura: facturaQueries[i]?.data }))
        .filter((x): x is { servicio: ServicioInscritoResponse; factura: FacturaResponse } => !!x.factura && x.factura.estado !== "PAGADA" && x.factura.estado !== "ANULADA"),
    [servicios, facturaQueries],
  );

  const [cuentaId, setCuentaId] = useState<string>("");
  const [seleccion, setSeleccion] = useState<Set<string>>(new Set(servicioPreseleccionado ? [servicioPreseleccionado] : []));
  const [resultado, setResultado] = useState<PagoResponse[] | null>(null);

  const cuentaActiva = cuentaId || cuentas.find((c) => c.predeterminada)?.id || cuentas[0]?.id || "";

  const registrarPago = useRegistrarPago();
  const registrarPagoLote = useRegistrarPagoLote();

  const seleccionados = pendientes.filter((p) => seleccion.has(p.servicio.id));
  const total = seleccionados.reduce((sum, p) => sum + p.factura.montoTotal, 0);

  const toggle = (servicioId: string) => {
    setSeleccion((prev) => {
      const next = new Set(prev);
      next.has(servicioId) ? next.delete(servicioId) : next.add(servicioId);
      return next;
    });
  };

  const pagar = async () => {
    if (!cuentaActiva || seleccionados.length === 0) return;
    try {
      if (seleccionados.length === 1) {
        const pago = await registrarPago.mutateAsync({ cuentaId: cuentaActiva, facturaId: seleccionados[0].factura.id });
        setResultado([pago]);
      } else {
        const pagos = await registrarPagoLote.mutateAsync({ cuentaId: cuentaActiva, facturaIds: seleccionados.map((s) => s.factura.id) });
        setResultado(pagos);
      }
      setSeleccion(new Set());
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos procesar el pago"), "error");
    }
  };

  if (resultado) {
    return <ComprobantePago pagos={resultado} onNuevoPago={() => setResultado(null)} />;
  }

  return (
    <div>
      <div className={styles.header}>
        <h1 style={{ fontSize: "1.4rem" }}>Pagar facturas</h1>
      </div>

      {cuentas.length === 0 ? (
        <EmptyState icon={Wallet} title="Necesitas una cuenta" description="Vincula una cuenta bancaria desde tu perfil antes de pagar." />
      ) : (
        <div className={styles.cuentaPicker}>
          {cuentas.map((cuenta) => (
            <button
              key={cuenta.id}
              type="button"
              className={`${styles.cuentaChip} ${cuentaActiva === cuenta.id ? styles.active : ""}`}
              onClick={() => setCuentaId(cuenta.id)}
            >
              <div className={styles.cuentaChipTitle}>{cuenta.alias || cuenta.numeroCuentaEnmascarado}</div>
              <div className={`${styles.cuentaChipMeta} money`}>{formatMoney(cuenta.saldo)} disponible</div>
            </button>
          ))}
        </div>
      )}

      <Section title="Elige qué pagar">
        {pendientes.length === 0 ? (
          <EmptyState icon={ReceiptText} title="No tienes facturas pendientes" description="Cuando tengas una factura por pagar aparecerá aquí." />
        ) : (
          <div className={styles.list}>
            {pendientes.map(({ servicio, factura }) => {
              const visual = categoriaVisual(categoriaPorEmpresa.get(servicio.empresaId) ?? servicio.empresaRazonSocial);
              return (
                <label key={servicio.id} className={styles.checkRow}>
                  <input type="checkbox" className={styles.checkbox} checked={seleccion.has(servicio.id)} onChange={() => toggle(servicio.id)} />
                  <visual.icon size={18} color={visual.color} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontWeight: 600 }}>{servicio.alias || servicio.empresaRazonSocial}</div>
                    <div style={{ fontSize: "0.82rem", color: "var(--ink-soft)" }}>{dueLabel(factura.fechaVencimiento)}</div>
                  </div>
                  <span className="money" style={{ fontWeight: 700 }}>{formatMoney(factura.montoTotal)}</span>
                </label>
              );
            })}
          </div>
        )}
      </Section>

      {seleccionados.length > 0 && (
        <div className={styles.summaryBar}>
          <div>
            <div style={{ fontSize: "0.8rem", color: "var(--ink-soft)" }}>{seleccionados.length} factura{seleccionados.length > 1 ? "s" : ""}</div>
            <div className="money" style={{ fontWeight: 700, fontSize: "1.1rem" }}>{formatMoney(total)}</div>
          </div>
          <Button variant="copper" onClick={pagar} loading={registrarPago.isPending || registrarPagoLote.isPending} disabled={!cuentaActiva}>
            Confirmar pago
          </Button>
        </div>
      )}
    </div>
  );
}

function ComprobantePago({ pagos, onNuevoPago }: { pagos: PagoResponse[]; onNuevoPago: () => void }) {
  const total = pagos.reduce((sum, p) => sum + p.monto, 0);

  const descargarPdf = async (pago: PagoResponse) => {
    const blob = await api.getBlob(`/api/pagos/${pago.id}/comprobante/pdf`);
    downloadBlob(blob, `comprobante-${pago.codigoComprobante}.pdf`);
  };

  return (
    <div className={`${styles.comprobante} settle-in`}>
      <CheckCircle2 size={56} color="var(--success)" strokeWidth={1.5} />
      <h1 style={{ fontSize: "1.3rem" }}>Pago exitoso</h1>
      <span className={styles.comprobanteAmount}>{formatMoney(total)}</span>

      <div style={{ width: "100%", display: "flex", flexDirection: "column", gap: "var(--space-3)", marginTop: "var(--space-4)" }}>
        {pagos.map((pago) => (
          <div key={pago.id} className={styles.checkRow} style={{ textAlign: "left" }}>
            <ReceiptText size={18} />
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600 }}>{pago.codigoComprobante}</div>
              <div className={styles.comprobanteMeta}>{formatMoney(pago.monto)}</div>
            </div>
            <Button size="sm" variant="secondary" onClick={() => descargarPdf(pago)}>
              <Download size={15} /> PDF
            </Button>
          </div>
        ))}
      </div>

      <Button variant="primary" onClick={onNuevoPago} style={{ marginTop: "var(--space-5)" }}>
        Volver a pagos
      </Button>
    </div>
  );
}
