import { z } from "zod";

// Reglas espejo del backend: StrongPasswordValidator (mín. 8, 1 mayúscula, 1 número).
export const passwordSchema = z
  .string()
  .min(8, "Mínimo 8 caracteres")
  .regex(/[A-Z]/, "Debe incluir al menos una mayúscula")
  .regex(/\d/, "Debe incluir al menos un número");

export const loginSchema = z.object({
  email: z.string().email("Correo inválido"),
  password: z.string().min(1, "Ingresa tu contraseña"),
});
export type LoginForm = z.infer<typeof loginSchema>;

export const registroSchema = z.object({
  documentoIdentidad: z.string().min(1, "Requerido"),
  tipoDocumento: z.string().min(1, "Requerido"),
  nombre: z.string().min(1, "Requerido"),
  apellido: z.string().min(1, "Requerido"),
  email: z.string().email("Correo inválido"),
  telefono: z.string().optional(),
  password: passwordSchema,
});
export type RegistroForm = z.infer<typeof registroSchema>;

export const vincularCuentaSchema = z.object({
  numeroCuenta: z.string().min(4, "Ingresa un número de cuenta válido"),
  tipo: z.enum(["AHORROS", "CORRIENTE"]),
});
export type VincularCuentaForm = z.infer<typeof vincularCuentaSchema>;

export const inscribirServicioSchema = z.object({
  empresaId: z.string().min(1, "Elige una empresa"),
  numeroReferencia: z.string().min(1, "Requerido"),
  alias: z.string().optional(),
});
export type InscribirServicioForm = z.infer<typeof inscribirServicioSchema>;

export const perfilSchema = z.object({
  nombre: z.string().min(1, "Requerido"),
  apellido: z.string().min(1, "Requerido"),
  telefono: z.string().optional(),
  email: z.string().email("Correo inválido"),
});
export type PerfilForm = z.infer<typeof perfilSchema>;

export const reversionSchema = z.object({
  motivo: z.string().min(5, "Cuéntanos por qué (mínimo 5 caracteres)"),
});
export type ReversionForm = z.infer<typeof reversionSchema>;
