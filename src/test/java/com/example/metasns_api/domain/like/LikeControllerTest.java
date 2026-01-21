package com.example.metasns_api.domain.like;

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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(LikeController.class)
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LikeService likeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void get_like_count_success() throws Exception{
        Long postId = 1L;
        when(likeService.getLikeCount(postId)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/likes/{postId}",postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(5)));

        verify(likeService).getLikeCount(postId);
    }

    @Test
    void like_success() throws Exception{

        Long postId = 1L;
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 10L);

        mockMvc.perform(
                post("/api/v1/posts/{postId}/like",postId)
                        .requestAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isAccepted());

        verify(likeService).like(postId, 10L);
    }

    @Test
    void like_unauthorized() throws Exception{
        Long postId = 1L;

        mockMvc.perform(post("/api/v1/posts/{postId}/like",postId))
                .andExpect(status().isUnauthorized());

        verify(likeService, never()).like(any(), any());
    }

    @Test
    void unlike_success() throws Exception{
        Long postId = 1L;
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 10L);

        mockMvc.perform(
                delete("/api/v1/posts/{postId}/like", postId)
                        .requestAttr("user", user)
            )
                .andExpect(status().isAccepted());

        verify(likeService).unlike(postId, 10L);
    }

    @Test
    void unlike_unauthorized() throws Exception{
        Long postId = 1L;

        mockMvc.perform(delete("/api/v1/posts/{postId}/like", postId))
                .andExpect(status().isUnauthorized());

        verify(likeService, never()).unlike(any(), any());
    }
}
