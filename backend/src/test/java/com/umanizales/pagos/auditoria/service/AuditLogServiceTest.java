package com.umanizales.pagos.auditoria.service;

import com.umanizales.pagos.auditoria.entity.AuditLog;
import com.umanizales.pagos.auditoria.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void registrarPersisteUnAuditLogConLosCamposEsperados() {
        AuditLogService service = new AuditLogService(auditLogRepository);
        UUID usuarioId = UUID.randomUUID();
        UUID entidadId = UUID.randomUUID();

        service.registrar(usuarioId, "PAGO_REGISTRADO", "Pago", entidadId, "detalle de prueba");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog guardado = captor.getValue();
        assertThat(guardado.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(guardado.getAccion()).isEqualTo("PAGO_REGISTRADO");
        assertThat(guardado.getEntidadTipo()).isEqualTo("Pago");
        assertThat(guardado.getEntidadId()).isEqualTo(entidadId);
        assertThat(guardado.getDetalle()).isEqualTo("detalle de prueba");
        assertThat(guardado.getFechaHora()).isNotNull();
        // Fuera de un request HTTP (como en este test unitario) la IP queda null.
        assertThat(guardado.getDireccionIp()).isNull();
    }

    @Test
    void registrarEnNuevaTransaccionTambienPersisteElLog() {
        AuditLogService service = new AuditLogService(auditLogRepository);

        service.registrarEnNuevaTransaccion(null, "LOGIN_FALLIDO", "Cliente", null, "correo desconocido");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getAccion()).isEqualTo("LOGIN_FALLIDO");
    }
}
