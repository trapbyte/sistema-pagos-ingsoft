import type { ButtonHTMLAttributes, ElementType, ComponentPropsWithoutRef } from "react";
import { Loader2 } from "lucide-react";
import styles from "./Button.module.css";

type Variant = "primary" | "copper" | "secondary" | "ghost" | "danger";

interface OwnProps {
  variant?: Variant;
  full?: boolean;
  size?: "md" | "sm";
  loading?: boolean;
  as?: ElementType;
}

type ButtonProps = OwnProps &
  Omit<ButtonHTMLAttributes<HTMLButtonElement>, keyof OwnProps> &
  Omit<ComponentPropsWithoutRef<ElementType>, keyof OwnProps | "children">;

export function Button({
  variant = "secondary",
  full,
  size = "md",
  loading,
  as,
  className,
  children,
  disabled,
  ...rest
}: ButtonProps) {
  const Component = as ?? "button";
  const extra = as ? {} : { disabled: disabled || loading };

  return (
    <Component
      className={[styles.btn, styles[variant], full ? styles.full : "", size === "sm" ? styles.sm : "", className ?? ""].join(" ")}
      {...extra}
      {...rest}
    >
      {loading && <Loader2 size={16} className="spin" aria-hidden />}
      {children}
    </Component>
  );
}
