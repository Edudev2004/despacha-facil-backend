package com.despachafacil.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTeamMemberRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        String fullName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato de correo no es válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe contener al menos una letra y un número")
        String password,

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(regexp = "^(ROLE_OPERATOR|ROLE_DISPATCHER|ROLE_ADMIN)$", message = "El rol debe ser ROLE_OPERATOR, ROLE_DISPATCHER o ROLE_ADMIN")
        String role
) {}
