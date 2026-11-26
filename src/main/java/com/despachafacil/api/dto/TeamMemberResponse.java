package com.despachafacil.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TeamMemberResponse(
        UUID id,
        UUID tenantId,
        String fullName,
        String email,
        String role,
        Boolean isActive,
        OffsetDateTime createdAt
) {}
