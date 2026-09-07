import { useQueries } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { CreditCard, PlusCircle, ReceiptText, Wallet } from "lucide-react";
import { useAuth } from "../../auth/AuthContext";
import { useCuentas } from "../../hooks/useCuentas";
import { useEmpresas, useServicios } from "../../hooks/useServicios";
import { api } from "../../lib/apiClient";
import { formatMoney, dueLabel } from "../../lib/format";
import { categoriaVisual } from "../../lib/serviceCategory";
import { Section } from "../../components/Card";
import { BillStub } from "../../components/Card";
import { Button } from "../../components/Button";
import { EmptyState } from "../../components/EmptyState";
import type { FacturaResponse, ServicioInscritoResponse } from "../../types/api";
import styles from "./DashboardPage.module.css";

export function DashboardPage() {
  const { perfil } = useAuth();
  const navigate = useNavigate();
  const { data: cuentas = [], isLoading: cargandoCuentas } = useCuentas();
  const { data: servicios = [], isLoading: cargandoServicios } = useServicios();
  const { data: empresas = [] } = useEmpresas();
  const categoriaPorEmpresa = new Map(empresas.map((e) => [e.id, e.categoria]));

  const facturaQueries = useQueries({
    queries: servicios.map((servicio) => ({
      queryKey: ["servicios", servicio.id, "factura"],
      queryFn: () => api.get<FacturaResponse>(`/api/servicios/${servicio.id}/factura`),
      staleTime: 60_000,
    })),
  });

  const saldoTotal = cuentas.reduce((sum, c) => sum + c.saldo, 0);

  const pendientes = servicios
    .map((servicio, i) => ({ servicio, factura: facturaQueries[i]?.data }))
    .filter((x): x is { servicio: ServicioInscritoResponse; factura: FacturaResponse } => !!x.factura && x.factura.estado !== "PAGADA" && x.factura.estado !== "ANULADA")
    .sort((a, b) => a.factura.fechaVencimiento.localeCompare(b.factura.fechaVencimiento));

  const cargando = cargandoCuentas || cargandoServicios;

  return (
    <div>
      <div className={styles.hero}>
        <p className={styles.greeting}>Hola{perfil ? `, ${perfil.nombre}` : ""}</p>
        <span className={styles.balanceLabel}>Saldo disponible en tus cuentas</span>
        <span className={`${styles.balance} money`}>{cargandoCuentas ? "—" : formatMoney(saldoTotal)}</span>
        <div className={styles.quickActions}>
          <Button variant="copper" onClick={() => navigate("/pagar")}>
            <CreditCard size={16} /> Pagar una factura
          </Button>
          <Button variant="secondary" onClick={() => navigate("/servicios")}>
            <PlusCircle size={16} /> Inscribir servicio
          </Button>
        </div>
      </div>

      <Section title="Facturas pendientes">
        {cargando ? null : pendientes.length === 0 ? (
          <EmptyState icon={ReceiptText} title="Estás al día" description="No tienes facturas pendientes por pagar en este momento." />
        ) : (
          <div className={styles.list}>
            {pendientes.map(({ servicio, factura }) => {
              const visual = categoriaVisual(categoriaPorEmpresa.get(servicio.empresaId) ?? servicio.empresaRazonSocial);
              return (
                <BillStub
                  key={servicio.id}
                  icon={<visual.icon size={18} />}
                  accent={visual.color}
                  title={servicio.alias || servicio.empresaRazonSocial}
                  meta={`${dueLabel(factura.fechaVencimiento)} · ${servicio.empresaRazonSocial}`}
                  amount={formatMoney(factura.montoTotal)}
                  action={
                    <Button size="sm" variant="primary" onClick={() => navigate(`/pagar?servicioId=${servicio.id}`)}>
                      Pagar
                    </Button>
                  }
                />
              );
            })}
          </div>
        )}
      </Section>

      <Section title="Tus cuentas">
        {cargandoCuentas ? null : cuentas.length === 0 ? (
          <EmptyState icon={Wallet} title="Aún no tienes cuentas" description="Vincula una cuenta bancaria para empezar a pagar tus servicios." action={<Button variant="primary" onClick={() => navigate("/perfil")}>Vincular cuenta</Button>} />
        ) : (
          <div className={styles.list}>
            {cuentas.map((cuenta) => (
              <BillStub
                key={cuenta.id}
                icon={<Wallet size={18} />}
                accent="var(--teal)"
                title={cuenta.alias || cuenta.numeroCuentaEnmascarado}
                meta={`${cuenta.tipo === "AHORROS" ? "Ahorros" : "Corriente"} · ${cuenta.numeroCuentaEnmascarado}`}
                amount={formatMoney(cuenta.saldo)}
              />
            ))}
          </div>
        )}
      </Section>
    </div>
  );
}
