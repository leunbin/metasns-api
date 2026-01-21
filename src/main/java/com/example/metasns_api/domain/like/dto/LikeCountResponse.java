package com.example.metasns_api.domain.like.dto;

public record LikeCountResponse(
        Long postId,
        long likeCount
) {}
