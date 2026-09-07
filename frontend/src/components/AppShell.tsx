import type { ReactNode } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { Home, Zap, CreditCard, History, User, LogOut, ShieldCheck, RotateCcw } from "lucide-react";
import { useAuth } from "../auth/AuthContext";
import styles from "./AppShell.module.css";

const NAV_ITEMS = [
  { to: "/panel", label: "Inicio", icon: Home, end: true },
  { to: "/servicios", label: "Servicios", icon: Zap },
  { to: "/pagar", label: "Pagar", icon: CreditCard },
  { to: "/historial", label: "Historial", icon: History },
  { to: "/perfil", label: "Perfil", icon: User },
];

export function AppShell({ children }: { children: ReactNode }) {
  const { logout, perfil, session } = useAuth();
  const navigate = useNavigate();
  const esAdmin = session?.rol === "ADMINISTRADOR";

  const handleLogout = () => {
    logout();
    navigate("/ingresar", { replace: true });
  };

  const sidebarItems = esAdmin
    ? [...NAV_ITEMS.slice(0, 4), { to: "/reversiones", label: "Reversiones", icon: RotateCcw }, { to: "/auditoria", label: "Auditoría", icon: ShieldCheck }, NAV_ITEMS[4]]
    : [...NAV_ITEMS.slice(0, 4), { to: "/reversiones", label: "Reversiones", icon: RotateCcw }, NAV_ITEMS[4]];

  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar}>
        <div className={styles.brand}>
          <span className={styles.brandMark} aria-hidden>
            <CreditCard size={16} />
          </span>
          Recibo
        </div>
        <nav className={styles.sidebarNav}>
          {sidebarItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `${styles.sidebarItem} ${isActive ? styles.active : ""}`}
            >
              <item.icon size={18} /> {item.label}
            </NavLink>
          ))}
        </nav>
        <button type="button" className={styles.logout} onClick={handleLogout} style={{ marginTop: "auto" }}>
          <LogOut size={18} /> {perfil ? `Salir · ${perfil.nombre}` : "Salir"}
        </button>
      </aside>

      <div style={{ flex: 1, minWidth: 0 }}>
        <header className={`${styles.topbar} ${styles.topbarInline}`}>
          <div className={styles.brand}>
            <span className={styles.brandMark} aria-hidden>
              <CreditCard size={16} />
            </span>
            Recibo
          </div>
          <button type="button" className={styles.logout} onClick={handleLogout} aria-label="Cerrar sesión">
            <LogOut size={18} />
          </button>
        </header>

        <main className={styles.main}>
          <div className={styles.contentInner}>{children}</div>
        </main>
      </div>

      <nav className={styles.bottomNav} aria-label="Navegación principal">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) => `${styles.navItem} ${isActive ? styles.active : ""}`}
          >
            <item.icon size={20} />
            {item.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
