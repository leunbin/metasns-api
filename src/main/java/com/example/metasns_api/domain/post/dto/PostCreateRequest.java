package com.example.metasns_api.domain.post.dto;

import lombok.Getter;

@Getter
public class PostCreateRequest {
    private String title;
    private String textContent;

    public PostCreateRequest(String title, String textContent){
        this.title = title;
        this.textContent = textContent;
    }
}
