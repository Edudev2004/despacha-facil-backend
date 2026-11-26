package com.despachafacil.api.dto;

public record SlugCheckResponse(
        String slug,
        boolean available
) {}
