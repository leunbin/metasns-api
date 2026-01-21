package com.example.metasns_api.domain.content.dto;

public record ContentUploadEvent(
        Long contentId,
        byte[] fileData,
        String originalFilename,
        String contentType
) {}
