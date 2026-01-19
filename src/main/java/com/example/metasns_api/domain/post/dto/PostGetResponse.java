package com.example.metasns_api.domain.post.dto;

import com.example.metasns_api.domain.post.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostGetResponse {
    private Long id;
    private Long authorId;
    private String title;
    private String textContent;
    private LocalDateTime createdAt;

    private PostGetResponse(
            Long id,
            Long authorId,
            String title,
            String textContent,
            LocalDateTime createdAt
    ){
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.textContent = textContent;
        this.createdAt = createdAt;
    }

    public static PostGetResponse from(Post post){
        return new PostGetResponse(
                post.getId(),
                post.getAuthorId(),
                post.getTitle(),
                post.getTextContent(),
                post.getCreatedAt()
        );
    }
}
