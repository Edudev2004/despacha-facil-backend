package com.despachafacil.api.controller;

import com.despachafacil.api.dto.TenantSettingsResponse;
import com.despachafacil.api.dto.UpdateTenantSettingsRequest;
import com.despachafacil.api.service.TenantSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenant/settings")
public class TenantSettingsController {

    private final TenantSettingsService tenantSettingsService;

    public TenantSettingsController(TenantSettingsService tenantSettingsService) {
        this.tenantSettingsService = tenantSettingsService;
    }

    @GetMapping
    public ResponseEntity<TenantSettingsResponse> getSettings() {
        return ResponseEntity.ok(tenantSettingsService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantSettingsResponse> updateSettings(
            @Valid @RequestBody UpdateTenantSettingsRequest request
    ) {
        return ResponseEntity.ok(tenantSettingsService.updateSettings(request));
    }
}
