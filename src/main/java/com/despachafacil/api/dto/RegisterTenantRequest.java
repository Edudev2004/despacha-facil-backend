package com.despachafacil.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterTenantRequest(
        @NotBlank(message = "El nombre de la tienda es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
        String storeName,

        @NotBlank(message = "El subdominio (slug) es obligatorio")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "El slug solo permite letras minúsculas, números y guiones medios")
        @Size(min = 3, max = 100, message = "El slug debe tener entre 3 y 100 caracteres")
        String slug,

        @NotBlank(message = "El número de WhatsApp es obligatorio")
        @Size(min = 9, max = 30, message = "El número de WhatsApp no es válido")
        String whatsappNumber,

        @NotBlank(message = "El nombre del administrador es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
        String adminFullName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no es válido")
        String adminEmail,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        String businessType
) {}
