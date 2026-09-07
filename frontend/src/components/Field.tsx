import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from "react";
import { forwardRef } from "react";
import styles from "./Field.module.css";

interface WrapperProps {
  label: string;
  error?: string;
  hint?: string;
  children: ReactNode;
  htmlFor?: string;
}

function FieldWrapper({ label, error, hint, children, htmlFor }: WrapperProps) {
  return (
    <label className={styles.field} htmlFor={htmlFor}>
      <span className={styles.label}>{label}</span>
      {children}
      {error ? <span className={styles.error}>{error}</span> : hint ? <span className={styles.hint}>{hint}</span> : null}
    </label>
  );
}

interface InputFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  hint?: string;
}

export const InputField = forwardRef<HTMLInputElement, InputFieldProps>(({ label, error, hint, id, className, ...rest }, ref) => (
  <FieldWrapper label={label} error={error} hint={hint} htmlFor={id}>
    <input ref={ref} id={id} className={[styles.input, error ? styles.hasError : "", className ?? ""].join(" ")} {...rest} />
  </FieldWrapper>
));
InputField.displayName = "InputField";

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  error?: string;
  hint?: string;
}

export const SelectField = forwardRef<HTMLSelectElement, SelectFieldProps>(({ label, error, hint, id, className, children, ...rest }, ref) => (
  <FieldWrapper label={label} error={error} hint={hint} htmlFor={id}>
    <select ref={ref} id={id} className={[styles.select, error ? styles.hasError : "", className ?? ""].join(" ")} {...rest}>
      {children}
    </select>
  </FieldWrapper>
));
SelectField.displayName = "SelectField";

interface TextareaFieldProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
  error?: string;
  hint?: string;
}

export const TextareaField = forwardRef<HTMLTextAreaElement, TextareaFieldProps>(({ label, error, hint, id, className, ...rest }, ref) => (
  <FieldWrapper label={label} error={error} hint={hint} htmlFor={id}>
    <textarea ref={ref} id={id} className={[styles.textarea, error ? styles.hasError : "", className ?? ""].join(" ")} rows={3} {...rest} />
  </FieldWrapper>
));
TextareaField.displayName = "TextareaField";
