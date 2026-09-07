import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate } from "react-router-dom";
import { ChevronRight, FlaskConical, RotateCcw, ShieldCheck, Star, Trash2, Wallet, Zap } from "lucide-react";
import { useAuth } from "../../auth/AuthContext";
import { api } from "../../lib/apiClient";
import { formatMoney } from "../../lib/format";
import { perfilSchema, vincularCuentaSchema, type PerfilForm, type VincularCuentaForm } from "../../lib/schemas";
import { useActualizarCuenta, useCuentas, useDepositoPrueba, useDesvincularCuenta, useVincularCuenta } from "../../hooks/useCuentas";
import { Section } from "../../components/Card";
import { Button } from "../../components/Button";
import { InputField, SelectField } from "../../components/Field";
import { Modal } from "../../components/Modal";
import { EmptyState } from "../../components/EmptyState";
import { useToast, apiErrorMessage } from "../../components/Toast";
import type { ActualizarPerfilRequest, ClientePerfilResponse } from "../../types/api";
import styles from "./PerfilPage.module.css";

export function PerfilPage() {
  const { perfil, refrescarPerfil, logout, session } = useAuth();
  const navigate = useNavigate();
  const notify = useToast();
  const [modalVincular, setModalVincular] = useState(false);
  const [cuentaADepositar, setCuentaADepositar] = useState<{ id: string; nombre: string } | null>(null);
  const { data: cuentas = [] } = useCuentas();
  const actualizarCuenta = useActualizarCuenta();
  const desvincular = useDesvincularCuenta();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting, isDirty },
  } = useForm<PerfilForm>({ resolver: zodResolver(perfilSchema) });

  useEffect(() => {
    if (perfil) reset({ nombre: perfil.nombre, apellido: perfil.apellido, telefono: perfil.telefono ?? "", email: perfil.email });
  }, [perfil, reset]);

  const guardarPerfil = async (data: PerfilForm) => {
    try {
      await api.patch<ClientePerfilResponse>("/api/clientes/me", data satisfies ActualizarPerfilRequest);
      await refrescarPerfil();
      notify("Perfil actualizado", "success");
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos actualizar tu perfil"), "error");
    }
  };

  const marcarPredeterminada = async (cuentaId: string) => {
    try {
      await actualizarCuenta.mutateAsync({ cuentaId, data: { predeterminada: true } });
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos actualizar la cuenta"), "error");
    }
  };

  const eliminarCuenta = async (cuentaId: string) => {
    if (!confirm("¿Desvincular esta cuenta?")) return;
    try {
      await desvincular.mutateAsync(cuentaId);
      notify("Cuenta desvinculada", "success");
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos desvincular la cuenta"), "error");
    }
  };

  const darDeBaja = async () => {
    if (!confirm("Esto inactivará tu cuenta de Recibo. ¿Continuar?")) return;
    try {
      await api.delete("/api/clientes/me");
      notify("Tu cuenta fue dada de baja", "info");
      logout();
      navigate("/ingresar", { replace: true });
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos dar de baja tu cuenta"), "error");
    }
  };

  return (
    <div>
      <div className={styles.header}>
        <h1 style={{ fontSize: "1.4rem" }}>Tu perfil</h1>
      </div>

      <Section title="Datos personales">
        <form className={styles.form} onSubmit={handleSubmit(guardarPerfil)} noValidate>
          <div className={styles.row}>
            <InputField label="Nombre" error={errors.nombre?.message} {...register("nombre")} />
            <InputField label="Apellido" error={errors.apellido?.message} {...register("apellido")} />
          </div>
          <InputField label="Correo" type="email" error={errors.email?.message} {...register("email")} />
          <InputField label="Teléfono" type="tel" error={errors.telefono?.message} {...register("telefono")} />
          <Button type="submit" variant="primary" loading={isSubmitting} disabled={!isDirty}>
            Guardar cambios
          </Button>
        </form>
      </Section>

      <Section title="Cuentas bancarias" action={<Button size="sm" variant="secondary" onClick={() => setModalVincular(true)}>Vincular</Button>}>
        {cuentas.length === 0 ? (
          <EmptyState icon={Wallet} title="Sin cuentas vinculadas" description="Vincula una cuenta de ahorros o corriente para poder pagar." />
        ) : (
          <div className={styles.linkList}>
            {cuentas.map((cuenta) => (
              <div key={cuenta.id} className={styles.cuentaCard}>
                <Wallet size={20} color="var(--teal)" />
                <div className={styles.cuentaInfo}>
                  <div className={styles.cuentaTitle}>{cuenta.alias || cuenta.numeroCuentaEnmascarado}</div>
                  <div className={styles.cuentaMeta}>
                    {cuenta.tipo === "AHORROS" ? "Ahorros" : "Corriente"} · {cuenta.numeroCuentaEnmascarado} · <span className="money">{formatMoney(cuenta.saldo)}</span>
                  </div>
                </div>
                <Button
                  size="sm"
                  variant="ghost"
                  title="Agregar saldo de prueba (solo desarrollo)"
                  onClick={() => setCuentaADepositar({ id: cuenta.id, nombre: cuenta.alias || cuenta.numeroCuentaEnmascarado })}
                >
                  <FlaskConical size={15} />
                </Button>
                <button type="button" className={`${styles.star} ${cuenta.predeterminada ? styles.active : ""}`} title="Predeterminada" onClick={() => marcarPredeterminada(cuenta.id)}>
                  <Star size={18} fill={cuenta.predeterminada ? "currentColor" : "none"} />
                </button>
                <Button size="sm" variant="danger" onClick={() => eliminarCuenta(cuenta.id)} aria-label="Desvincular cuenta">
                  <Trash2 size={15} />
                </Button>
              </div>
            ))}
          </div>
        )}
      </Section>

      <Section title="Más opciones">
        <div className={styles.linkList}>
          <Link className={styles.linkRow} to="/domiciliacion">
            <span style={{ display: "flex", alignItems: "center", gap: "var(--space-3)" }}>
              <Zap size={18} /> Domiciliación automática
            </span>
            <ChevronRight size={18} />
          </Link>
          <Link className={styles.linkRow} to="/reversiones">
            <span style={{ display: "flex", alignItems: "center", gap: "var(--space-3)" }}>
              <RotateCcw size={18} /> Reversiones de pago
            </span>
            <ChevronRight size={18} />
          </Link>
          {session?.rol === "ADMINISTRADOR" && (
            <Link className={styles.linkRow} to="/auditoria">
              <span style={{ display: "flex", alignItems: "center", gap: "var(--space-3)" }}>
                <ShieldCheck size={18} /> Auditoría del sistema
              </span>
              <ChevronRight size={18} />
            </Link>
          )}
        </div>
      </Section>

      <Section title="Zona de riesgo">
        <div className={styles.dangerZone}>
          <div>
            <p style={{ fontWeight: 600 }}>Dar de baja tu cuenta</p>
            <p style={{ fontSize: "0.85rem", color: "var(--ink-soft)" }}>Podrás volver a registrarte más adelante.</p>
          </div>
          <Button variant="danger" onClick={darDeBaja}>
            Dar de baja
          </Button>
        </div>
      </Section>

      {modalVincular && <VincularCuentaModal onClose={() => setModalVincular(false)} />}
      {cuentaADepositar && <DepositoPruebaModal cuenta={cuentaADepositar} onClose={() => setCuentaADepositar(null)} />}
    </div>
  );
}

/**
 * Solo para desarrollo/pruebas: no existe en el documento de especificación (el saldo
 * real lo gestiona el Core Bancario). Permite probar pagos sin acceso directo a la BD.
 */
function DepositoPruebaModal({ cuenta, onClose }: { cuenta: { id: string; nombre: string }; onClose: () => void }) {
  const depositar = useDepositoPrueba();
  const notify = useToast();
  const [monto, setMonto] = useState("100000");

  const confirmar = async () => {
    const valor = Number(monto);
    if (!valor || valor <= 0) {
      notify("Ingresa un monto válido", "error");
      return;
    }
    try {
      await depositar.mutateAsync({ cuentaId: cuenta.id, data: { monto: valor } });
      notify(`Saldo de prueba agregado a ${cuenta.nombre}`, "success");
      onClose();
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos agregar el saldo"), "error");
    }
  };

  return (
    <Modal title={`Saldo de prueba · ${cuenta.nombre}`} onClose={onClose}>
      <div className={styles.form}>
        <p style={{ fontSize: "0.85rem", color: "var(--ink-soft)" }}>
          Función solo de desarrollo: acredita saldo directamente para poder probar pagos. No existe en un banco real.
        </p>
        <InputField label="Monto a agregar" type="number" min={1} step={1000} value={monto} onChange={(e) => setMonto(e.target.value)} />
        <Button variant="primary" full onClick={confirmar} loading={depositar.isPending}>
          Agregar saldo
        </Button>
      </div>
    </Modal>
  );
}

function VincularCuentaModal({ onClose }: { onClose: () => void }) {
  const vincular = useVincularCuenta();
  const notify = useToast();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<VincularCuentaForm>({ resolver: zodResolver(vincularCuentaSchema), defaultValues: { tipo: "AHORROS" } });

  const onSubmit = async (data: VincularCuentaForm) => {
    try {
      await vincular.mutateAsync(data);
      notify("Cuenta vinculada", "success");
      onClose();
    } catch (err) {
      notify(apiErrorMessage(err, "No pudimos vincular la cuenta"), "error");
    }
  };

  return (
    <Modal title="Vincular cuenta bancaria" onClose={onClose}>
      <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
        <InputField label="Número de cuenta" error={errors.numeroCuenta?.message} {...register("numeroCuenta")} />
        <SelectField label="Tipo de cuenta" error={errors.tipo?.message} {...register("tipo")}>
          <option value="AHORROS">Ahorros</option>
          <option value="CORRIENTE">Corriente</option>
        </SelectField>
        <Button type="submit" variant="primary" full loading={isSubmitting}>
          Vincular
        </Button>
      </form>
    </Modal>
  );
}
