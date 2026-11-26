package com.despachafacil.api.dto;

import java.util.UUID;

public record UserSessionResponse(
        UUID userId,
        UUID tenantId,
        String tenantSlug,
        String tenantName,
        String fullName,
        String email,
        String role,
        String token
) {}
