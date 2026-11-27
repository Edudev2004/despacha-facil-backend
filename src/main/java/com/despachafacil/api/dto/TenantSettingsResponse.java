package com.despachafacil.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TenantSettingsResponse(
        UUID id,
        String name,
        String slug,
        String businessType,
        String whatsappNumber,
        String currency,
        String logoUrl,
        Boolean logoHasBorder,
        String primaryColor,
        String bannerUrl,
        String yapeQrUrl,
        String yapePhone,
        String plinPhone,
        BigDecimal freeShippingThreshold,
        Boolean isActive
) {}
