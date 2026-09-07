import styles from "./Badge.module.css";

type Tone = "success" | "danger" | "warning" | "neutral" | "teal";

const TONE_STYLE: Record<Tone, { bg: string; fg: string }> = {
  success: { bg: "var(--success-tint)", fg: "var(--success)" },
  danger: { bg: "var(--danger-tint)", fg: "var(--danger)" },
  warning: { bg: "var(--warning-tint)", fg: "var(--warning)" },
  neutral: { bg: "var(--neutral-tint)", fg: "var(--ink-soft)" },
  teal: { bg: "var(--teal-tint)", fg: "var(--teal-strong)" },
};

const ESTADO_MAP: Record<string, { label: string; tone: Tone }> = {
  // Factura
  PENDIENTE: { label: "Pendiente", tone: "warning" },
  PAGADA: { label: "Pagada", tone: "success" },
  VENCIDA: { label: "Vencida", tone: "danger" },
  ANULADA: { label: "Anulada", tone: "neutral" },
  // Pago
  PROCESANDO: { label: "Procesando", tone: "warning" },
  EXITOSO: { label: "Exitoso", tone: "success" },
  RECHAZADO: { label: "Rechazado", tone: "danger" },
  REVERSADO: { label: "Reversado", tone: "neutral" },
  // Reversión
  SOLICITADA: { label: "Solicitada", tone: "warning" },
  APROBADA: { label: "Aprobada", tone: "success" },
  RECHAZADA: { label: "Rechazada", tone: "danger" },
  EJECUTADA: { label: "Ejecutada", tone: "success" },
  // Cuenta / cliente / domiciliación
  ACTIVA: { label: "Activa", tone: "success" },
  ACTIVO: { label: "Activo", tone: "success" },
  INACTIVA: { label: "Inactiva", tone: "neutral" },
  INACTIVO: { label: "Inactivo", tone: "neutral" },
  BLOQUEADA: { label: "Bloqueada", tone: "danger" },
};

export function EstadoBadge({ estado }: { estado: string }) {
  const info = ESTADO_MAP[estado] ?? { label: estado, tone: "neutral" as Tone };
  const { bg, fg } = TONE_STYLE[info.tone];
  return (
    <span className={styles.badge} style={{ background: bg, color: fg }}>
      <span className={styles.dot} />
      {info.label}
    </span>
  );
}
