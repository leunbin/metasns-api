package com.example.metasns_api.domain.like;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"postId", "userId"})
        }
)
@NoArgsConstructor
public class Like {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "postId")
    private Long postId;

    @Column
    private LocalDateTime createdAt;

    @Builder
    public Like(Long userId, Long postId){
        this.userId = userId;
        this.postId = postId;
        this.createdAt = LocalDateTime.now();
    }
}
