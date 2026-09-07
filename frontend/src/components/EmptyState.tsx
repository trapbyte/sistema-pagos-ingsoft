import type { LucideIcon } from "lucide-react";
import type { ReactNode } from "react";

export function EmptyState({ icon: Icon, title, description, action }: { icon: LucideIcon; title: string; description: string; action?: ReactNode }) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        textAlign: "center",
        gap: "var(--space-3)",
        padding: "var(--space-7) var(--space-4)",
        color: "var(--ink-soft)",
      }}
    >
      <Icon size={32} strokeWidth={1.5} />
      <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
        <p style={{ color: "var(--ink)", fontWeight: 600 }}>{title}</p>
        <p style={{ fontSize: "0.88rem", maxWidth: "34ch" }}>{description}</p>
      </div>
      {action}
    </div>
  );
}
