package com.example.metasns_api.domain.content;


import com.example.metasns_api.common.minio.MinioInitializer;
import com.example.metasns_api.domain.auth.TokenProvider;
import com.example.metasns_api.domain.user.User;
import com.example.metasns_api.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContentIntegrationTest {
    @MockitoBean
    MinioService minioService;

    @MockitoBean
    MinioInitializer minioInitializer;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Test
    void uploadingContents_success() throws Exception{
        User user = userRepository.save(
                new User("test@test.com", "password")
        );

        contentRepository.save(
                Content.builder()
                        .postId(1L)
                        .uploaderId(user.getId())
                        .fileName("test.png")
                        .objectKey("contents/test.png")
                        .fileSize(100L)
                        .contentType("image/png")
                        .build()
        );

        String token = tokenProvider.generatedTokenDTO(user).getAccessToken();

        mockMvc.perform(get("/api/v1/me/contents/uploading")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void upload_success() throws Exception{
        User user = userRepository.save(
                new User("test@test.com", "password")
        );

        given(minioService.uploadImage(any(byte[].class), anyString(), anyString()))
                .willReturn("contents/images/test.png");

        String token = tokenProvider.generatedTokenDTO(user).getAccessToken();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/post/{postId}/contents",1L)
                .file(file)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        assertThat(contentRepository.findAll()).hasSize(1);
    }
}
