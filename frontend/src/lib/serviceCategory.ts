import { Droplet, Flame, Wifi, Zap, Receipt, type LucideIcon } from "lucide-react";

export interface CategoriaVisual {
  color: string;
  icon: LucideIcon;
  label: string;
}

const CATEGORIAS: Record<string, CategoriaVisual> = {
  AGUA: { color: "var(--servicio-agua)", icon: Droplet, label: "Agua" },
  ACUEDUCTO: { color: "var(--servicio-agua)", icon: Droplet, label: "Agua" },
  ENERGIA: { color: "var(--servicio-energia)", icon: Zap, label: "Energía" },
  ELECTRIC: { color: "var(--servicio-energia)", icon: Zap, label: "Energía" },
  GAS: { color: "var(--servicio-gas)", icon: Flame, label: "Gas" },
  INTERNET: { color: "var(--servicio-internet)", icon: Wifi, label: "Internet" },
};

/** La categoría llega como texto libre del backend (EmpresaServicio.categoria); se
 * normaliza para elegir un color/ícono representativo. Si no coincide con ninguna
 * categoría conocida, cae a un tratamiento neutral (no se inventa una categoría). */
export function categoriaVisual(categoria: string): CategoriaVisual {
  const normalizada = categoria
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .toUpperCase();

  for (const [key, visual] of Object.entries(CATEGORIAS)) {
    if (normalizada.includes(key)) return visual;
  }
  return { color: "var(--servicio-otro)", icon: Receipt, label: categoria };
}
