package com.despachafacil.api.security;

import com.despachafacil.api.domain.TenantAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Validador de seguridad de aislamiento multi-tenant (RF-02).
 * Verifica que las operaciones sobre entidades de negocio coincidan estrictamente
 * con el tenant del usuario autenticado en TenantContext.
 */
@Component
public class TenantValidator {

    private static final Logger log = LoggerFactory.getLogger(TenantValidator.class);

    /**
     * Valida que la entidad pertenezca al tenant en el contexto actual.
     * Si no coincide, lanza ResponseStatusException con HTTP 403 Forbidden y registra la alerta.
     */
    public void validateTenantAccess(TenantAware entity) {
        if (entity == null) {
            return;
        }

        UUID currentTenantId = TenantContext.getTenantId();
        if (currentTenantId == null) {
            log.warn("⚠️ [SECURITY] Intento de acceso a recurso TenantAware sin tenantId en TenantContext");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso no autorizado: contexto de comercio ausente");
        }

        UUID entityTenantId = entity.getTenantId();
        if (entityTenantId != null && !currentTenantId.equals(entityTenantId)) {
            log.error("🚨 [VIOLACIÓN DE AISLAMIENTO MULTI-TENANT] El tenant '{}' intentó acceder al recurso del tenant '{}'",
                    currentTenantId, entityTenantId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: no tienes permisos para acceder a recursos de otro comercio");
        }
    }

    /**
     * Valida que un UUID de tenant explícito coincida con el tenant en sesión.
     */
    public void validateTenantId(UUID requestedTenantId) {
        if (requestedTenantId == null) {
            return;
        }

        UUID currentTenantId = TenantContext.getTenantId();
        if (currentTenantId == null || !currentTenantId.equals(requestedTenantId)) {
            log.error("🚨 [VIOLACIÓN DE AISLAMIENTO MULTI-TENANT] Tenant en sesión '{}' difiere del solicitado '{}'",
                    currentTenantId, requestedTenantId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado a los datos de este comercio");
        }
    }
}
