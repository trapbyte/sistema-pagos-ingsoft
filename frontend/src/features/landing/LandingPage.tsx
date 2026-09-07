import { useEffect, useState, type ReactNode } from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  CalendarClock,
  CreditCard,
  Droplet,
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
          <span className={styles.eyebrowless}>Servicios públicos, un solo lugar</span>
          <h1 className={styles.headline}>Todas tus facturas, pagadas antes de que las olvides</h1>
          <p className={styles.subhead}>
            Vincula tus cuentas, inscribe agua, luz, gas e internet, y paga cada factura —o actívalas para que se
            paguen solas— desde un mismo lugar, con comprobante al instante.
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
          <PhoneMockup />
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

function PhoneMockup() {
  const facturas = [
    { icon: Droplet, accent: "var(--servicio-agua)", title: "Aguas de Manizales", meta: "Vence en 3 días", amount: "$ 62.400" },
    { icon: Zap, accent: "var(--servicio-energia)", title: "CHEC", meta: "Vence en 6 días", amount: "$ 138.500" },
    { icon: Wifi, accent: "var(--servicio-internet)", title: "Internet hogar", meta: "Vence en 12 días", amount: "$ 89.900" },
  ];

  return (
    <div className={styles.phone}>
      <div className={styles.phoneScreen}>
        <div className={styles.phoneStatus}>
          <span>9:41</span>
          <span>●●●●</span>
        </div>
        <div className={styles.phoneBalance}>
          <span className={styles.phoneBalanceLabel}>Saldo disponible</span>
          <span className={`${styles.phoneBalanceValue} money`}>$ 1.245.300</span>
        </div>
        {facturas.map((f) => (
          <div key={f.title} className={styles.phoneStub} style={{ ["--stub-accent" as string]: f.accent }}>
            <span className={styles.phoneStubIcon}>
              <f.icon size={15} />
            </span>
            <div className={styles.phoneStubBody}>
              <span className={styles.phoneStubTitle}>{f.title}</span>
              <span className={styles.phoneStubMeta}>{f.meta}</span>
            </div>
            <span className={`${styles.phoneStubAmount} money`}>{f.amount}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
