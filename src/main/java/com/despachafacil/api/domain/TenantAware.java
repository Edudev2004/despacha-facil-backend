package com.despachafacil.api.domain;

import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Interfaz base para todas las entidades que pertenecen a un tenant específico.
 * Garantiza el cumplimiento estricto del aislamiento multi-tenant (RF-02).
 */
public interface TenantAware {
    Tenant getTenant();
    void setTenant(Tenant tenant);

    default UUID getTenantId() {
        return getTenant() != null ? getTenant().getId() : null;
    }
}
