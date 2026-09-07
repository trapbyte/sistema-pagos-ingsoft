import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate } from "react-router-dom";
import { Receipt } from "lucide-react";
import { useAuth } from "../../auth/AuthContext";
import { loginSchema, type LoginForm } from "../../lib/schemas";
import { InputField } from "../../components/Field";
import { Button } from "../../components/Button";
import { ApiRequestError } from "../../lib/apiClient";
import styles from "./Auth.module.css";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });

  const onSubmit = async (data: LoginForm) => {
    setServerError(null);
    try {
      await login(data);
      navigate("/panel", { replace: true });
    } catch (err) {
      setServerError(err instanceof ApiRequestError ? err.message : "No pudimos iniciar sesión. Intenta de nuevo.");
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.brand}>
          <span className={styles.brandMark}>
            <Receipt size={18} />
          </span>
        </div>
        <h1 className={styles.title}>Bienvenido de vuelta</h1>
        <p className={styles.subtitle}>Ingresa para ver tus facturas y pagar sin filas.</p>

        <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
          {serverError && <div className={styles.formError}>{serverError}</div>}
          <InputField label="Correo" type="email" autoComplete="email" error={errors.email?.message} {...register("email")} />
          <InputField label="Contraseña" type="password" autoComplete="current-password" error={errors.password?.message} {...register("password")} />
          <Button type="submit" variant="primary" full loading={isSubmitting}>
            Ingresar
          </Button>
        </form>

        <p className={styles.footer}>
          ¿No tienes cuenta? <Link to="/registrarse">Crea una</Link>
        </p>
      </div>
    </div>
  );
}
