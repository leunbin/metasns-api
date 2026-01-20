package com.example.metasns_api.domain.post;

import com.example.metasns_api.common.exception.PostException;
import com.example.metasns_api.domain.post.dto.PostCreateRequest;
import com.example.metasns_api.domain.post.dto.PostGetResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostService postService;

    @Test
    void getPostById_success(){
        Long postId = 1L;

        Post post = Post.builder()
                .authorId(10L)
                .title("test title")
                .textContent("test content")
                .build();

        ReflectionTestUtils.setField(post, "id", postId);

        given(postRepository.findById(postId))
                .willReturn(Optional.of(post));

        PostGetResponse response = postService.getPostById(postId);

        assertThat(response.getId()).isEqualTo(postId);
        assertThat(response.getTitle()).isEqualTo("test title");
        assertThat(response.getTextContent()).isEqualTo("test content");
    }

    @Test
    void getPostId_notFound(){
        Long postId = 1L;

        given(postRepository.findById(postId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(PostException.class)
                .hasMessage("게시물이 존재하지 않습니다.");
    }

    @Test
    void create_success(){
        Long userId = 10L;

        PostCreateRequest request = new PostCreateRequest(
                "제목",
                "본문"
        );

        Post created = Post.builder()
                .title("제목")
                .textContent("본문")
                .authorId(userId)
                .build();

        ReflectionTestUtils.setField(created, "id", 100L);

        given(postRepository.save(any(Post.class)))
                .willReturn(created);

        Long postId = postService.createPost(request, userId);

        assertThat(postId).isEqualTo(100L);

    }
}
