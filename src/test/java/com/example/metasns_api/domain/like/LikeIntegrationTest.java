package com.example.metasns_api.domain.like;

import com.example.metasns_api.common.minio.MinioInitializer;
import com.example.metasns_api.domain.content.MinioService;
import com.example.metasns_api.domain.user.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
class LikeIntegrationTest extends IntegrationTestSupport{
    @Autowired
    MockMvc mockMvc;

    @Autowired
    LikeRepository likeRepository;

    @MockitoBean
    MinioService minioService;

    @MockitoBean
    MinioInitializer minioInitializer;

    @Test
    void like_flow() throws Exception{
        Long postId = 1L;
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 100);

        mockMvc.perform(
                post("/api/v1/posts/{postId}/like", postId)
                        .requestAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isAccepted());

        mockMvc.perform(
                get("/api/v1/likes/{postId}", postId)
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));

        mockMvc.perform(
                delete("/api/v1/posts/{postId}/like", postId)
                        .requestAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isAccepted());

        mockMvc.perform(
                        get("/api/v1/likes/{postId}", postId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
    }
}
