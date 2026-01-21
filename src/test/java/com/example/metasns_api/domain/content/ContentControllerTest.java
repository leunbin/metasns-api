package com.example.metasns_api.domain.content;

import com.example.metasns_api.domain.content.dto.DownloadUrlResponse;
import com.example.metasns_api.domain.user.User;
import com.example.metasns_api.domain.user.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@Tag("unit")
class ContentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ContentService contentService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getContestByPostId_success() throws Exception{
        Long postId = 1L;

        given(contentService.getContentsByPostId(postId))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/post/{postId}/content", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(contentService).getContentsByPostId(postId);
    }

    @Test
    void uploadingContents_success() throws Exception{
        User user = new User("test.test.com", "encodedPassword");
        ReflectionTestUtils.setField(user,"id",10L);

        given(contentService.getContentsInUploading(10L))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/me/contents/uploading")
                .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(contentService).getContentsInUploading(10L);
    }

    @Test
    void uploadingContents_fail_when_unauthorized() throws Exception{
        mockMvc.perform(get("/api/v1/me/contents/uploading"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(contentService);
    }

    @Test
    void upload_success() throws Exception{
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 10L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/post/{postId}/contents",1L)
                .file(file)
                .requestAttr("user",user))
                .andExpect(status().isAccepted());

        verify(contentService).requestUpload(any(MultipartFile.class), eq(1L), eq(10L));
    }

    @Test
    void upload_fail_when_unauthorized() throws Exception{
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/post/{postId}/contents", 1L)
                .file(file))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(contentService);
    }

    @Test
    void download_success() throws Exception {
        given(contentService.getDownloadUrl(1L))
                .willReturn(new DownloadUrlResponse("http://download"));

        mockMvc.perform(get("/api/v1/content/{contentId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.downloadUrl")
                        .value("http://download"));

        verify(contentService).getDownloadUrl(1L);
    }
}
