package com.example.metasns_api.domain.post;

import com.example.metasns_api.common.exception.PostException;
import com.example.metasns_api.domain.post.dto.PostCreateRequest;
import com.example.metasns_api.domain.post.dto.PostGetResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration TTL = Duration.ofHours(1);

    public PostGetResponse getPostById(Long postId){
        String key = "post:" + postId;

        //1. redis 조회
        String cached = stringRedisTemplate.opsForValue().get(key);

        if(cached != null){
            try{
                return objectMapper.readValue(cached, PostGetResponse.class);
            } catch (Exception e){
                // 파싱 실패 시 캐시 제거
                stringRedisTemplate.delete(key);
            }
        }

        //2. DB 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(()->
                        new PostException(
                                HttpStatus.NOT_FOUND,
                                "게시물이 존재하지 않습니다."
                        )
                );

        PostGetResponse response = PostGetResponse.from(post);

        //3. redis 저장
        try{
            String json = objectMapper.writeValueAsString(response);
            stringRedisTemplate.opsForValue().set(key, json, TTL);
        } catch (Exception e){
            //무시
        }

        return response;
    }

    @Transactional
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
