package com.example.metasns_api.domain.post;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long authorId;

    @Column
    private String title;

    @Column
    private String textContent;

    @Column
    private LocalDateTime createdAt;

    @Builder
    public Post(Long authorId, String title, String textContent){
        this.authorId = authorId;
        this.title = title;
        this.textContent = textContent;
        this.createdAt = LocalDateTime.now();

    }
}
