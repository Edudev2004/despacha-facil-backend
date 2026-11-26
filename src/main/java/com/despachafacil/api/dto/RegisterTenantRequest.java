package com.despachafacil.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterTenantRequest(
        @NotBlank(message = "El nombre de la tienda es obligatorio")
        @Size(min = 3, max = 150, message = "El nombre de la tienda debe tener entre 3 y 150 caracteres")
        String storeName,

        @NotBlank(message = "El subdominio (slug) es obligatorio")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "El slug solo permite letras minúsculas, números y guiones medios")
        @Size(min = 3, max = 100, message = "El slug debe tener entre 3 y 100 caracteres")
        String slug,

        @NotBlank(message = "El número de WhatsApp es obligatorio")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "El número de WhatsApp debe estar en formato internacional estándar E.164 (ej. +51921620680)")
        String whatsappNumber,

        @NotBlank(message = "El nombre del administrador es obligatorio")
        @Size(min = 5, max = 150, message = "Ingresa tu nombre y apellido completo (mínimo 5 caracteres)")
        String adminFullName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "El correo electrónico no es válido")
        String adminEmail,

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "La contraseña debe tener mínimo 8 caracteres, incluyendo al menos una letra y un número")
        String password,

        String businessType
) {}
