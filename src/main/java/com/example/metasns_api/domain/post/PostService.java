package com.example.metasns_api.domain.post;

import com.example.metasns_api.common.exception.PostException;
import com.example.metasns_api.domain.post.dto.PostCreateRequest;
import com.example.metasns_api.domain.post.dto.PostGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;

    public PostGetResponse getPostById(Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(()->
                        new PostException(
                                HttpStatus.NOT_FOUND,
                                "게시물이 존재하지 않습니다."
                        )
                );

        return PostGetResponse.from(post);
    }

    public Long createPost(PostCreateRequest request, Long userId){
        Post post = Post.builder()
                .title(request.getTitle())
                .authorId(userId)
                .textContent(request.getTextContent())
                .build();

        Post created = postRepository.save(post);

        return created.getId();
    }
}
