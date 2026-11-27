package com.despachafacil.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateTenantSettingsRequest(
        @NotBlank(message = "El nombre de la tienda es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        String name,

        @NotBlank(message = "El número de WhatsApp es obligatorio")
        @Size(max = 30, message = "El número de WhatsApp no puede exceder 30 caracteres")
        String whatsappNumber,

        @Size(max = 50, message = "El tipo de negocio no puede exceder 50 caracteres")
        String businessType,

        @Size(max = 500, message = "La URL del logo no puede exceder 500 caracteres")
        String logoUrl,

        Boolean logoHasBorder,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "El color primario debe ser un código hexadecimal válido (ej: #FFA800)")
        String primaryColor,

        @Size(max = 500, message = "La URL del banner no puede exceder 500 caracteres")
        String bannerUrl,

        @Size(max = 500, message = "La URL del QR de Yape no puede exceder 500 caracteres")
        String yapeQrUrl,

        @Size(max = 30, message = "El número celular de Yape no puede exceder 30 caracteres")
        String yapePhone,

        @Size(max = 30, message = "El número celular de Plin no puede exceder 30 caracteres")
        String plinPhone,

        @PositiveOrZero(message = "El umbral de envío gratis debe ser mayor o igual a cero")
        BigDecimal freeShippingThreshold
) {}
