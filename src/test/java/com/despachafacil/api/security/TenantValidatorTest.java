package com.despachafacil.api.security;

import com.despachafacil.api.domain.Tenant;
import com.despachafacil.api.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantValidatorTest {

    private TenantValidator tenantValidator;
    private UUID tenantAId;
    private UUID tenantBId;

    @BeforeEach
    void setUp() {
        tenantValidator = new TenantValidator();
        tenantAId = UUID.randomUUID();
        tenantBId = UUID.randomUUID();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("[RF-02] Permite acceso cuando el tenantId coincide con el recurso")
    void shouldAllowAccessWhenTenantMatches() {
        TenantContext.setTenantId(tenantAId);

        Tenant tenant = new Tenant();
        tenant.setId(tenantAId);

        User user = new User();
        user.setTenant(tenant);

        assertDoesNotThrow(() -> tenantValidator.validateTenantAccess(user));
        assertDoesNotThrow(() -> tenantValidator.validateTenantId(tenantAId));
    }

    @Test
    @DisplayName("[RF-02] Bloquea con 403 Forbidden cuando se intenta acceder a recurso de otro tenant")
    void shouldThrowForbiddenWhenAccessingAnotherTenantResource() {
        TenantContext.setTenantId(tenantAId);

        Tenant tenantB = new Tenant();
        tenantB.setId(tenantBId);

        User userFromTenantB = new User();
        userFromTenantB.setTenant(tenantB);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> tenantValidator.validateTenantAccess(userFromTenantB)
        );

        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("otro comercio"));
    }

    @Test
    @DisplayName("[RF-02] Bloquea cuando no hay tenant en contexto")
    void shouldThrowForbiddenWhenContextIsEmpty() {
        Tenant tenantA = new Tenant();
        tenantA.setId(tenantAId);

        User user = new User();
        user.setTenant(tenantA);

        assertThrows(
                ResponseStatusException.class,
                () -> tenantValidator.validateTenantAccess(user)
        );
    }
}
