import { useEffect, useState, type ReactNode } from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  CalendarClock,
  CreditCard,
  Droplet,
  Flame,
  History,
  Link2,
  Receipt,
  RotateCcw,
  Wifi,
  Zap,
} from "lucide-react";
import { Button } from "../../components/Button";
import { useReveal } from "../../hooks/useReveal";
import styles from "./LandingPage.module.css";

const CAPACIDADES = [
  {
    icon: CreditCard,
    accent: "var(--teal)",
    title: "Pago inmediato o en lote",
    desc: "Paga una factura al momento o varias a la vez desde la misma cuenta, en un solo paso.",
  },
  {
    icon: CalendarClock,
    accent: "var(--servicio-energia)",
    title: "Domiciliación automática",
    desc: "Activa el pago automático y olvídate de las fechas de vencimiento.",
  },
  {
    icon: History,
    accent: "var(--servicio-internet)",
    title: "Historial y comprobantes",
    desc: "Filtra tus pagos por fecha, cuenta o estado y descarga el comprobante en PDF.",
  },
  {
    icon: RotateCcw,
    accent: "var(--copper)",
    title: "Reversión de pagos",
    desc: "¿Un cobro por error? Solicita la reversión hasta 24 horas después de pagar.",
  },
];

const PASOS = [
  { title: "Vincula tu cuenta", desc: "Conecta tu cuenta de ahorros o corriente en menos de un minuto." },
  { title: "Inscribe tus servicios", desc: "Agua, luz, gas o internet — cada uno con su número de referencia." },
  { title: "Paga en segundos", desc: "Revisa el monto, confirma y listo. Tu comprobante queda guardado." },
];

export function LandingPage() {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 8);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <div className={styles.page}>
      <header className={`${styles.nav} ${scrolled ? styles.navScrolled : ""}`}>
        <div className={styles.brand}>
          <span className={styles.brandMark}>
            <Receipt size={16} />
          </span>
          Recibo
        </div>
        <div className={styles.navActions}>
          <Button variant="ghost" size="sm" as={Link} to="/ingresar">
            Iniciar sesión
          </Button>
          <Button variant="primary" size="sm" as={Link} to="/registrarse">
            Crear cuenta
          </Button>
        </div>
      </header>

      <section className={styles.hero}>
        <div className={`${styles.heroCopy} ${styles.in}`}>
          <span className={styles.eyebrowless}>Agua, luz, gas e internet — un solo lugar</span>
          <h1 className={styles.headline}>Todas tus facturas, pagadas antes de que las olvides</h1>
          <p className={styles.subhead}>
            Vincula tu cuenta, inscribe cada servicio y paga en segundos —o déjalos domiciliados y olvídate de las
            fechas—. Un comprobante te espera cada vez.
          </p>
          <div className={styles.heroActions}>
            <Button variant="copper" as={Link} to="/registrarse">
              Crear cuenta gratis <ArrowRight size={16} />
            </Button>
            <Button variant="secondary" as={Link} to="/ingresar">
              Ya tengo cuenta
            </Button>
          </div>
          <p className={styles.heroNote}>Sin costos ocultos. Tus datos bancarios nunca se comparten con terceros.</p>
        </div>

        <div className={`${styles.heroVisual} ${styles.in}`}>
          <HeroVisual />
        </div>
      </section>

      <Reveal>
        <div className={styles.sectionHead}>
          <h2 className={styles.sectionTitle}>Todo lo que necesitas para no atrasarte</h2>
          <p className={styles.sectionSubtitle}>Cuatro herramientas pensadas para el día a día de pagar servicios.</p>
        </div>
        <div className={styles.grid4}>
          {CAPACIDADES.map((c) => (
            <div key={c.title} className={styles.featureCard}>
              <span className={styles.featureIcon} style={{ ["--feature-accent" as string]: c.accent }}>
                <c.icon size={20} />
              </span>
              <span className={styles.featureTitle}>{c.title}</span>
              <span className={styles.featureDesc}>{c.desc}</span>
            </div>
          ))}
        </div>
      </Reveal>

      <Reveal>
        <div className={styles.sectionHead}>
          <h2 className={styles.sectionTitle}>Cómo funciona</h2>
          <p className={styles.sectionSubtitle}>Tres pasos, la primera vez. Después, solo entrar y pagar.</p>
        </div>
        <div className={styles.steps}>
          {PASOS.map((paso, i) => (
            <div key={paso.title} className={styles.step}>
              <span className={styles.stepNumber}>{String(i + 1).padStart(2, "0")}</span>
              <span className={styles.stepTitle}>{paso.title}</span>
              <span className={styles.stepDesc}>{paso.desc}</span>
            </div>
          ))}
        </div>
      </Reveal>

      <Reveal>
        <div className={styles.cta}>
          <span style={{ display: "flex", gap: "var(--space-3)", color: "var(--copper)" }}>
            <Droplet size={22} /> <Zap size={22} /> <Link2 size={22} /> <Wifi size={22} />
          </span>
          <h2 className={styles.ctaTitle}>Deja de perseguir fechas de vencimiento</h2>
          <p className={styles.ctaSubtitle}>Crea tu cuenta gratis y vincula tu primer servicio en menos de dos minutos.</p>
          <Button variant="copper" as={Link} to="/registrarse">
            Crear cuenta gratis <ArrowRight size={16} />
          </Button>
        </div>
      </Reveal>

      <footer className={styles.footer}>
        <div className={styles.brand} style={{ fontSize: "0.95rem" }}>
          <span className={styles.brandMark} style={{ width: 24, height: 24 }}>
            <Receipt size={13} />
          </span>
          Recibo
        </div>
        <span>Proyecto académico — Ingeniería de Software · {new Date().getFullYear()}</span>
      </footer>
    </div>
  );
}

function Reveal({ children }: { children: ReactNode }) {
  const { ref, visible } = useReveal<HTMLElement>();
  return (
    <section ref={ref} className={`${styles.section} ${visible ? styles.in : ""}`}>
      {children}
    </section>
  );
}

interface Chip {
  icon: typeof Droplet;
  accent: string;
  top: number;
  left: number;
  size: number;
  rotate: number;
  delay: number;
}

const CENTER: Chip = { icon: CreditCard, accent: "var(--teal)", top: 50, left: 50, size: 30, rotate: 0, delay: 0 };

const SATELITES: Chip[] = [
  { icon: Droplet, accent: "var(--servicio-agua)", top: 16, left: 20, size: 21, rotate: -8, delay: 120 },
  { icon: Zap, accent: "var(--servicio-energia)", top: 12, left: 76, size: 19, rotate: 10, delay: 200 },
  { icon: Flame, accent: "var(--servicio-gas)", top: 80, left: 18, size: 19, rotate: 7, delay: 280 },
  { icon: Wifi, accent: "var(--servicio-internet)", top: 84, left: 74, size: 21, rotate: -9, delay: 360 },
];

/**
 * Composición visual del hero: en vez de una foto o una maqueta de pantalla, un
 * conjunto de "chips" flotantes con los íconos de cada servicio conectados a un centro
 * de pago — la idea de "todo en un solo lugar" hecha forma, sobre un fondo con
 * profundidad (dos manchas de color difuminadas + una grilla de puntos sutil).
 */
function HeroVisual() {
  const todos = [CENTER, ...SATELITES];

  return (
    <div className={styles.stage}>
      <div className={styles.stageGlowA} aria-hidden />
      <div className={styles.stageGlowB} aria-hidden />
      <div className={styles.stageDots} aria-hidden />

      <svg className={styles.stageLines} viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden>
        {SATELITES.map((s) => (
          <line
            key={`${s.top}-${s.left}`}
            x1={CENTER.left}
            y1={CENTER.top}
            x2={s.left}
            y2={s.top}
            stroke={s.accent}
            strokeWidth={0.4}
            strokeDasharray="2 2.5"
            opacity={0.45}
          />
        ))}
      </svg>

      {todos.map((chip, i) => (
        <span
          key={i}
          className={i === 0 ? `${styles.chip} ${styles.chipCenter}` : styles.chip}
          style={{
            top: `${chip.top}%`,
            left: `${chip.left}%`,
            width: `${chip.size}%`,
            ["--chip-accent" as string]: chip.accent,
            ["--chip-rotate" as string]: `${chip.rotate}deg`,
            ["--chip-delay" as string]: `${chip.delay}ms`,
          }}
        >
          <chip.icon size={i === 0 ? 26 : 18} strokeWidth={1.8} />
        </span>
      ))}
    </div>
  );
}
