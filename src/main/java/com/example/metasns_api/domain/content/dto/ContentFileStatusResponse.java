package com.example.metasns_api.domain.content.dto;

import com.example.metasns_api.domain.content.ContentStatus;

public record ContentFileStatusResponse(
        long postId,
        long uploaderId,
        String fileName,
        String objectKey,
        long fileSize,
        String contentType,
        ContentStatus status
) {}
