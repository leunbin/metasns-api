package com.example.metasns_api.domain.content;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "contents")
public class Content {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private long postId;

    @Column
    private long uploaderId;

    @Column
    private String fileName;

    @Column
    private String objectKey;

    @Column
    private long fileSize;

    @Column
    private String contentType;

    @Column
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column
    private ContentStatus status;

    @Column
    private String checksum;

    @Builder
    public Content(long postId, long uploaderId, String fileName, String objectKey, long fileSize, String contentType, String checksum){
        this.postId = postId;
        this.uploaderId = uploaderId;
        this.fileName = fileName;
        this.objectKey = objectKey;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.createdAt = LocalDateTime.now();
        this.status = ContentStatus.UPLOADING;
        this.checksum = checksum;
    }

    public void updateObjectKey(String objectKey){
        this.objectKey = objectKey;
    }

    public void changeAvailable(){
        this.status = ContentStatus.AVAILABLE;
    }

    public void changeFailed(){
        this.status = ContentStatus.FAILED;
    }

    public void changeDeleted(){
        this.status = ContentStatus.DELETED;
    }

}
