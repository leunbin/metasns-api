package com.example.metasns_api.domain.post;

import com.example.metasns_api.domain.post.dto.PostCreateRequest;
import com.example.metasns_api.domain.post.dto.PostGetResponse;
import com.example.metasns_api.domain.user.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Tag("unit")
class PostControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PostService postService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getPost_success() throws Exception{
        Long postId = 1L;

        Post post = Post.builder()
                .title("title")
                .authorId(10L)
                .textContent("content")
                .build();

        PostGetResponse response = PostGetResponse.from(post);

        ReflectionTestUtils.setField(response, "id", postId);

        given(postService.getPostById(postId))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.title").value("title"));

    }

    @Test
    void createPost_success() throws Exception{
        PostCreateRequest request = new PostCreateRequest("title", "content");

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 10L);

        given(postService.createPost(any(PostCreateRequest.class), eq(10L)))
                .willReturn(100L);

        mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("user", mockUser)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value(100L));
    }

    @Test
    void createPost_unauthenticated_fail() throws Exception{
        PostCreateRequest request = new PostCreateRequest("title", "content");

        mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isUnauthorized());
    }
}
