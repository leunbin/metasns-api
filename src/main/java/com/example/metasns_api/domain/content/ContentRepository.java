package com.example.metasns_api.domain.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    //id, 게시물 id, 상태로 찾기
    Optional<Content> findByIdAndPostIdAndStatus(Long id, Long postId, ContentStatus status);

    //게시물 id, 상태로 찾기
    List<Content> findByPostIdAndStatus(Long id, ContentStatus status);

    //업로더 id와 상태로 찾기
    List<Content> findByUploaderIdAndStatus(Long uploaderId, ContentStatus status);

}
