package com.despachafacil.api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTeamMemberRequest(
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        String fullName,

        @Pattern(regexp = "^(ROLE_OPERATOR|ROLE_DISPATCHER|ROLE_ADMIN)$", message = "El rol debe ser ROLE_OPERATOR, ROLE_DISPATCHER o ROLE_ADMIN")
        String role,

        Boolean isActive
) {}
