package com.example.metasns_api.domain.like.dto;

import com.example.metasns_api.domain.like.LikeEventType;

public record LikeEvent(
        Long postId,
        LikeEventType type
) {}
