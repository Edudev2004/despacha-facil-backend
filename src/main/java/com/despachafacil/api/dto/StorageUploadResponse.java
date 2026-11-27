package com.despachafacil.api.dto;

public record StorageUploadResponse(
        String url,
        String key,
        long size,
        String contentType
) {}
