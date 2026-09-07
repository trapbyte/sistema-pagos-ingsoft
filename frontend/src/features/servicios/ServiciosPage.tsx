import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate } from "react-router-dom";
import { Pencil, PlusCircle, Trash2, Zap } from "lucide-react";
import {
  useActualizarAliasServicio,
  useDesinscribirServicio,
  useEmpresas,
  useFactura,
  useInscribirServicio,
  useServicios,
} from "../../hooks/useServicios";
import { categoriaVisual } from "../../lib/serviceCategory";
import { formatMoney, dueLabel } from "../../lib/format";
import { inscribirServicioSchema, type InscribirServicioForm } from "../../lib/schemas";
import { Button } from "../../components/Button";
import { InputField, SelectField } from "../../components/Field";
import { Modal } from "../../components/Modal";
import { EstadoBadge } from "../../components/Badge";
import { EmptyState } from "../../components/EmptyState";
import { useToast, apiErrorMessage } from "../../components/Toast";
import type { ServicioInscritoResponse } from "../../types/api";
import styles from "./ServiciosPage.module.css";

export function ServiciosPage() {
  const { data: servicios = [], isLoading } = useServicios();
  const { data: empresas = [] } = useEmpresas();
  const [modalAbierto, setModalAbierto] = useState(false);

  return (
    <div>
      <div className={styles.header}>
        <h1 style={{ fontSize: "1.4rem" }}>Tus servicios</h1>
        <Button variant="primary" size="sm" onClick={() => setModalAbierto(true)}>
          <PlusCircle size={16} /> Inscribir
        </Button>
      </div>

      {!isLoading && servicios.length === 0 && (
        <EmptyState
          icon={Zap}
          title="Aún no has inscrito servicios"
          description="Inscribe agua, luz, gas o internet para empezar a ver y pagar tus facturas."
          action={<Button variant="primary" onClick={() => setModalAbierto(true)}>Inscribir un servicio</Button>}
        />
      )}

      <div className={styles.list}>
        {servicios.map((servicio) => (
          <ServicioRow key={servicio.id} servicio={servicio} categoria={empresas.find((e) => e.id === servicio.empresaId)?.categoria} />
        ))}
      </div>

      {modalAbierto && <InscribirServicioModal onClose={() => setModalAbierto(false)} />}
    </div>
  );
}

function ServicioRow({ servicio, categoria }: { servicio: ServicioInscritoResponse; categoria?: string }) {
  const { data: factura } = useFactura(servicio.id);
  const desinscribir = useDesinscribirServicio();
  const notify = useToast();
  const navigate = useNavigate();
  const [editando, setEditando] = useState(false);
  const visual = categoriaVisual(categoria ?? servicio.empresaRazonSocial);

  const eliminar = async () => {
    if (!confirm(`¿Desinscribir ${servicio.alias || servicio.empresaRazonSocial}?`)) return;
    try {
      await desinscribir.mutateAsync(servicio.id);
      notify("Servicio desinscrito", "success");
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos desinscribir el servicio"), "error");
    }
  };

  return (
    <div className={styles.servicioCard}>
      <div className={styles.servicioTop}>
        <span className={styles.servicioIcon} style={{ background: `color-mix(in srgb, ${visual.color} 15%, transparent)`, color: visual.color }}>
          <visual.icon size={18} />
        </span>
        <div className={styles.servicioInfo}>
          <div className={styles.servicioTitle}>{servicio.alias || servicio.empresaRazonSocial}</div>
          <div className={styles.servicioMeta}>{servicio.empresaRazonSocial} · Ref. {servicio.numeroReferencia}</div>
        </div>
        <div className={styles.servicioActions}>
          <Button size="sm" variant="ghost" onClick={() => setEditando(true)} aria-label="Editar alias">
            <Pencil size={15} />
          </Button>
          <Button size="sm" variant="danger" onClick={eliminar} aria-label="Desinscribir">
            <Trash2 size={15} />
          </Button>
        </div>
      </div>

      {factura && (
        <div className={styles.facturaRow}>
          <div>
            <EstadoBadge estado={factura.estado} /> <span style={{ color: "var(--ink-soft)", fontSize: "0.85rem" }}>{dueLabel(factura.fechaVencimiento)}</span>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: "var(--space-3)" }}>
            <span className="money" style={{ fontWeight: 700 }}>{formatMoney(factura.montoTotal)}</span>
            {factura.estado !== "PAGADA" && factura.estado !== "ANULADA" && (
              <Button size="sm" variant="copper" onClick={() => navigate(`/pagar?servicioId=${servicio.id}`)}>
                Pagar
              </Button>
            )}
          </div>
        </div>
      )}

      {editando && <EditarAliasModal servicio={servicio} onClose={() => setEditando(false)} />}
    </div>
  );
}

function InscribirServicioModal({ onClose }: { onClose: () => void }) {
  const { data: empresas = [] } = useEmpresas();
  const inscribir = useInscribirServicio();
  const notify = useToast();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<InscribirServicioForm>({ resolver: zodResolver(inscribirServicioSchema) });

  const onSubmit = async (data: InscribirServicioForm) => {
    try {
      await inscribir.mutateAsync(data);
      notify("Servicio inscrito", "success");
      onClose();
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos inscribir el servicio"), "error");
    }
  };

  return (
    <Modal title="Inscribir un servicio" onClose={onClose}>
      <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
        <SelectField label="Empresa" error={errors.empresaId?.message} {...register("empresaId")}>
          <option value="">Elige una empresa</option>
          {empresas.map((empresa) => (
            <option key={empresa.id} value={empresa.id}>
              {empresa.razonSocial} · {empresa.categoria}
            </option>
          ))}
        </SelectField>
        <InputField label="Número de referencia" hint="El número de cuenta o contrato con la empresa" error={errors.numeroReferencia?.message} {...register("numeroReferencia")} />
        <InputField label="Alias (opcional)" placeholder="Ej. Casa, Apto 302" error={errors.alias?.message} {...register("alias")} />
        <Button type="submit" variant="primary" full loading={isSubmitting}>
          Inscribir servicio
        </Button>
      </form>
    </Modal>
  );
}

function EditarAliasModal({ servicio, onClose }: { servicio: ServicioInscritoResponse; onClose: () => void }) {
  const [alias, setAlias] = useState(servicio.alias ?? "");
  const notify = useToast();
  const actualizar = useActualizarAliasServicio();

  const guardar = async () => {
    try {
      await actualizar.mutateAsync({ servicioId: servicio.id, data: { alias } });
      notify("Alias actualizado", "success");
      onClose();
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos actualizar el alias"), "error");
    }
  };

  return (
    <Modal title="Editar alias" onClose={onClose}>
      <div className={styles.form}>
        <InputField label="Alias" value={alias} onChange={(e) => setAlias(e.target.value)} placeholder={servicio.empresaRazonSocial} />
        <Button variant="primary" full onClick={guardar} loading={actualizar.isPending}>
          Guardar
        </Button>
      </div>
    </Modal>
  );
}
