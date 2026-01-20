package com.example.metasns_api.domain.content.dto;

public record ContentFileResponse(
        long uploaderId,
        String fileName,
        String objectKey,
        long fileSize,
        String contentType
) {}