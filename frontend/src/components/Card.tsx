import type { ReactNode } from "react";
import styles from "./Card.module.css";

export function Card({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={[styles.card, className ?? ""].join(" ")}>{children}</div>;
}

export function Section({ title, action, children }: { title: string; action?: ReactNode; children: ReactNode }) {
  return (
    <section className={styles.section}>
      <div className={styles.sectionHeader}>
        <h2 className={styles.sectionTitle}>{title}</h2>
        {action}
      </div>
      {children}
    </section>
  );
}

export function BillStub({
  icon,
  accent,
  title,
  meta,
  amount,
  action,
}: {
  icon: ReactNode;
  accent: string;
  title: string;
  meta: string;
  amount?: string;
  action?: ReactNode;
}) {
  return (
    <div className={styles.stub} style={{ ["--stub-accent" as string]: accent }}>
      <span className={styles.stubIcon} aria-hidden>
        {icon}
      </span>
      <div className={styles.stubBody}>
        <span className={styles.stubTitle}>{title}</span>
        <span className={styles.stubMeta}>{meta}</span>
      </div>
      <div className={styles.stubAmount}>
        {amount && <span className={`${styles.stubMoney} money`}>{amount}</span>}
        {action}
      </div>
    </div>
  );
}
