import type { ButtonHTMLAttributes } from "react";
import { Loader2 } from "lucide-react";
import styles from "./Button.module.css";

type Variant = "primary" | "copper" | "secondary" | "ghost" | "danger";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  full?: boolean;
  size?: "md" | "sm";
  loading?: boolean;
}

export function Button({ variant = "secondary", full, size = "md", loading, className, children, disabled, ...rest }: ButtonProps) {
  return (
    <button
      className={[styles.btn, styles[variant], full ? styles.full : "", size === "sm" ? styles.sm : "", className ?? ""].join(" ")}
      disabled={disabled || loading}
      {...rest}
    >
      {loading && <Loader2 size={16} className="spin" aria-hidden />}
      {children}
    </button>
  );
}
