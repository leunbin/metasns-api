package com.example.metasns_api.domain.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    //중복 좋아요 방지
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    //좋아요 취소용
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    //redis fallback
    long countByPostId(Long postId);
}
