import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate } from "react-router-dom";
import { Receipt } from "lucide-react";
import { useAuth } from "../../auth/AuthContext";
import { registroSchema, type RegistroForm } from "../../lib/schemas";
import { InputField, SelectField } from "../../components/Field";
import { Button } from "../../components/Button";
import { ApiRequestError } from "../../lib/apiClient";
import styles from "./Auth.module.css";

export function RegisterPage() {
  const { registrar } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegistroForm>({ resolver: zodResolver(registroSchema), defaultValues: { tipoDocumento: "CC" } });

  const onSubmit = async (data: RegistroForm) => {
    setServerError(null);
    try {
      await registrar(data);
      navigate("/", { replace: true });
    } catch (err) {
      setServerError(err instanceof ApiRequestError ? err.message : "No pudimos crear tu cuenta. Intenta de nuevo.");
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
        <h1 className={styles.title}>Crea tu cuenta</h1>
        <p className={styles.subtitle}>Vincula tus cuentas bancarias y paga tus servicios en minutos.</p>

        <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
          {serverError && <div className={styles.formError}>{serverError}</div>}
          <div className={styles.row}>
            <InputField label="Nombre" autoComplete="given-name" error={errors.nombre?.message} {...register("nombre")} />
            <InputField label="Apellido" autoComplete="family-name" error={errors.apellido?.message} {...register("apellido")} />
          </div>
          <div className={styles.row}>
            <SelectField label="Documento" error={errors.tipoDocumento?.message} {...register("tipoDocumento")}>
              <option value="CC">Cédula (CC)</option>
              <option value="CE">Cédula extranjería</option>
              <option value="PA">Pasaporte</option>
            </SelectField>
            <InputField label="N° de documento" error={errors.documentoIdentidad?.message} {...register("documentoIdentidad")} />
          </div>
          <InputField label="Correo" type="email" autoComplete="email" error={errors.email?.message} {...register("email")} />
          <InputField label="Teléfono (opcional)" type="tel" autoComplete="tel" error={errors.telefono?.message} {...register("telefono")} />
          <InputField
            label="Contraseña"
            type="password"
            autoComplete="new-password"
            hint="Mínimo 8 caracteres, una mayúscula y un número"
            error={errors.password?.message}
            {...register("password")}
          />
          <Button type="submit" variant="primary" full loading={isSubmitting}>
            Crear cuenta
          </Button>
        </form>

        <p className={styles.footer}>
          ¿Ya tienes cuenta? <Link to="/ingresar">Inicia sesión</Link>
        </p>
      </div>
    </div>
  );
}
