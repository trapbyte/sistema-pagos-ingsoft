// Tipos espejo de los DTOs y enums del backend (com.umanizales.pagos.*).
// Mantener sincronizado manualmente con las clases *Response/*Request del backend.

export type RolUsuario = "CLIENTE" | "ADMINISTRADOR";
export type EstadoCliente = "ACTIVO" | "INACTIVO";
export type TipoCuenta = "AHORROS" | "CORRIENTE";
export type EstadoCuenta = "ACTIVA" | "INACTIVA" | "BLOQUEADA";
export type EstadoFactura = "PENDIENTE" | "PAGADA" | "VENCIDA" | "ANULADA";
export type EstadoPago = "PROCESANDO" | "EXITOSO" | "RECHAZADO" | "REVERSADO";
export type TipoProcesamiento = "MANUAL" | "DOMICILIADO";
export type EstadoDomiciliacion = "ACTIVA" | "INACTIVA";
export type EstadoReversion = "SOLICITADA" | "APROBADA" | "RECHAZADA" | "EJECUTADA";

export interface ApiError {
  timestamp: string;
  status: number;
  message: string;
  errors: Record<string, string> | null;
}

// --- auth / cliente ---

export interface RegistroClienteRequest {
  documentoIdentidad: string;
  tipoDocumento: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresAt: string;
}

export interface ClientePerfilResponse {
  id: string;
  documentoIdentidad: string;
  tipoDocumento: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string | null;
  estado: EstadoCliente;
}

export interface ActualizarPerfilRequest {
  nombre: string;
  apellido: string;
  telefono?: string;
  email: string;
}

// --- cuentas ---

export interface VincularCuentaRequest {
  numeroCuenta: string;
  tipo: TipoCuenta;
}

export interface ActualizarPreferenciaCuentaRequest {
  alias?: string | null;
  predeterminada?: boolean | null;
}

/** Solo para desarrollo/pruebas — no existe en el documento de especificación. */
export interface DepositoPruebaRequest {
  monto: number;
}

export interface CuentaResponse {
  id: string;
  numeroCuentaEnmascarado: string;
  tipo: TipoCuenta;
  estado: EstadoCuenta;
  alias: string | null;
  predeterminada: boolean;
  saldo: number;
}

// --- empresas / servicios / facturas ---

export interface EmpresaServicioResponse {
  id: string;
  razonSocial: string;
  categoria: string;
}

export interface InscribirServicioRequest {
  empresaId: string;
  numeroReferencia: string;
  alias?: string;
}

export interface ActualizarAliasServicioRequest {
  alias?: string | null;
}

export interface ServicioInscritoResponse {
  id: string;
  empresaId: string;
  empresaRazonSocial: string;
  numeroReferencia: string;
  alias: string | null;
}

export interface FacturaResponse {
  id: string;
  numeroReferencia: string;
  montoTotal: number;
  fechaEmision: string;
  fechaVencimiento: string;
  estado: EstadoFactura;
}

// --- pagos ---

export interface RegistrarPagoRequest {
  cuentaId: string;
  facturaId: string;
  monto?: number;
}

export interface RegistrarPagoLoteRequest {
  cuentaId: string;
  facturaIds: string[];
}

export interface PagoResponse {
  id: string;
  cuentaId: string;
  facturaId: string;
  codigoComprobante: string;
  monto: number;
  estado: EstadoPago;
  tipoProcesamiento: TipoProcesamiento;
  fechaHora: string;
}

export interface PagoResumenResponse {
  id: string;
  codigoComprobante: string;
  monto: number;
  estado: EstadoPago;
  tipoProcesamiento: TipoProcesamiento;
  fechaHora: string;
}

export interface ComprobanteResponse {
  pagoId: string;
  codigoComprobante: string;
  clienteNombreCompleto: string;
  cuentaEnmascarada: string;
  empresaServicio: string;
  numeroReferencia: string;
  monto: number;
  fechaHora: string;
}

export interface FiltroHistorialPago {
  fechaDesde?: string;
  fechaHasta?: string;
  cuentaId?: string;
  tipoCuenta?: TipoCuenta;
  empresaId?: string;
  estado?: EstadoPago;
  montoMinimo?: number;
  montoMaximo?: number;
}

// --- domiciliación ---

export interface ActivarDomiciliacionRequest {
  servicioInscritoId: string;
  cuentaId: string;
}

export interface ActualizarDomiciliacionRequest {
  cuentaId?: string;
  estado?: EstadoDomiciliacion;
}

export interface DomiciliacionResponse {
  id: string;
  servicioInscritoId: string;
  numeroReferencia: string;
  cuentaId: string;
  cuentaEnmascarada: string;
  estado: EstadoDomiciliacion;
}

// --- reversiones ---

export interface SolicitarReversionRequest {
  pagoId: string;
  motivo: string;
}

export interface DecidirReversionRequest {
  aprobar: boolean;
  respuesta?: string;
}

export interface ReversionResponse {
  id: string;
  pagoId: string;
  codigoComprobantePago: string;
  fechaSolicitud: string;
  motivo: string;
  estado: EstadoReversion;
  respuestaAdministrador: string | null;
}

// --- auditoría ---

export interface AuditLogResponse {
  id: string;
  fechaHora: string;
  usuarioId: string | null;
  direccionIp: string | null;
  accion: string;
  entidadTipo: string | null;
  entidadId: string | null;
  detalle: string | null;
}
