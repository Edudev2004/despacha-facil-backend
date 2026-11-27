package com.despachafacil.api.service;

import com.despachafacil.api.domain.Tenant;
import com.despachafacil.api.dto.TenantSettingsResponse;
import com.despachafacil.api.dto.UpdateTenantSettingsRequest;
import com.despachafacil.api.repository.TenantRepository;
import com.despachafacil.api.security.TenantContext;
import com.despachafacil.api.security.TenantValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class TenantSettingsService {

    private static final Logger log = LoggerFactory.getLogger(TenantSettingsService.class);

    private final TenantRepository tenantRepository;
    private final TenantValidator tenantValidator;
    private final StorageService storageService;

    public TenantSettingsService(TenantRepository tenantRepository, TenantValidator tenantValidator, StorageService storageService) {
        this.tenantRepository = tenantRepository;
        this.tenantValidator = tenantValidator;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public TenantSettingsResponse getSettings() {
        UUID tenantId = TenantContext.getTenantId();
        tenantValidator.validateTenantId(tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comercio no encontrado"));

        log.debug("[TENANT][GET_SETTINGS] [Tenant: {}] Datos de configuración obtenidos", tenantId);
        return toResponse(tenant);
    }

    @Transactional
    public TenantSettingsResponse updateSettings(UpdateTenantSettingsRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        tenantValidator.validateTenantId(tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comercio no encontrado"));

        tenant.setName(request.name().trim());
        tenant.setWhatsappNumber(request.whatsappNumber().trim());

        if (request.businessType() != null && !request.businessType().isBlank()) {
            tenant.setBusinessType(request.businessType().trim());
        }

        // Si se cambia o elimina una imagen anterior, limpiar el archivo huérfano de Cloudflare R2
        String newLogo = request.logoUrl() != null && !request.logoUrl().isBlank() ? request.logoUrl().trim() : null;
        if (tenant.getLogoUrl() != null && !tenant.getLogoUrl().equals(newLogo)) {
            storageService.deleteFile(tenant.getLogoUrl());
        }
        tenant.setLogoUrl(newLogo);
        if (request.logoHasBorder() != null) {
            tenant.setLogoHasBorder(request.logoHasBorder());
        }

        tenant.setPrimaryColor(request.primaryColor() != null && !request.primaryColor().isBlank() ? request.primaryColor().trim() : "#FFA800");

        String newBanner = request.bannerUrl() != null && !request.bannerUrl().isBlank() ? request.bannerUrl().trim() : null;
        if (tenant.getBannerUrl() != null && !tenant.getBannerUrl().equals(newBanner)) {
            storageService.deleteFile(tenant.getBannerUrl());
        }
        tenant.setBannerUrl(newBanner);

        String newYapeQr = request.yapeQrUrl() != null && !request.yapeQrUrl().isBlank() ? request.yapeQrUrl().trim() : null;
        if (tenant.getYapeQrUrl() != null && !tenant.getYapeQrUrl().equals(newYapeQr)) {
            storageService.deleteFile(tenant.getYapeQrUrl());
        }
        tenant.setYapeQrUrl(newYapeQr);
        tenant.setYapePhone(request.yapePhone() != null && !request.yapePhone().isBlank() ? request.yapePhone().trim() : null);
        tenant.setPlinPhone(request.plinPhone() != null && !request.plinPhone().isBlank() ? request.plinPhone().trim() : null);
        tenant.setFreeShippingThreshold(request.freeShippingThreshold());

        Tenant updated = tenantRepository.save(tenant);
        log.info("[TENANT][UPDATE_SETTINGS] [Tenant: {}] Configuración de marca y billeteras actualizada exitosamente", tenantId);

        return toResponse(updated);
    }

    private TenantSettingsResponse toResponse(Tenant tenant) {
        return new TenantSettingsResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getBusinessType(),
                tenant.getWhatsappNumber(),
                tenant.getCurrency(),
                tenant.getLogoUrl(),
                tenant.getLogoHasBorder(),
                tenant.getPrimaryColor(),
                tenant.getBannerUrl(),
                tenant.getYapeQrUrl(),
                tenant.getYapePhone(),
                tenant.getPlinPhone(),
                tenant.getFreeShippingThreshold(),
                tenant.getIsActive()
        );
    }
}
