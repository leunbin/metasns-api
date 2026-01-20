package com.example.metasns_api.domain.content;

import com.example.metasns_api.common.exception.ContentException;
import com.example.metasns_api.common.exception.MinioException;
import com.example.metasns_api.common.exception.PostException;
import com.example.metasns_api.domain.content.dto.ContentFileResponse;
import com.example.metasns_api.domain.content.dto.ContentFileStatusResponse;
import com.example.metasns_api.domain.content.dto.DownloadUrlResponse;
import com.example.metasns_api.domain.post.Post;
import com.example.metasns_api.domain.post.PostRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    ContentRepository contentRepository;

    @Mock
    MinioService minioService;

    @Mock
    PostRepository postRepository;

    @InjectMocks
    ContentService contentService;

    @Test
    void getContentsByPostId_success(){
        Long postId = 1L;

        Content content = Content.builder()
                .postId(postId)
                .uploaderId(10L)
                .fileName("test.png")
                .objectKey("contents/images/test.png")
                .fileSize(1024L)
                .contentType("png")
                .build();

        content.changeAvailable();

        given(contentRepository.findByPostIdAndStatus(postId, ContentStatus.AVAILABLE))
                .willReturn(List.of(content));

        List<ContentFileResponse> contents = contentService.getContentsByPostId(postId);

        assertThat(contents).hasSize(1);
        assertThat(contents.get(0).objectKey()).isEqualTo("contents/images/test.png");
    }

    @Test
    void getContentsInUploading_success(){
        Long userId = 10L;

        Content content = Content.builder()
                .postId(1L)
                .uploaderId(userId)
                .fileName("test.png")
                .objectKey("contents/images/test.png")
                .fileSize(1024L)
                .contentType("png")
                .build();
        given(contentRepository.findByUploaderIdAndStatus(userId, ContentStatus.UPLOADING))
                .willReturn(List.of(content));

        List<ContentFileStatusResponse> contents = contentService.getContentsInUploading(userId);

        assertThat(contents).hasSize(1);
        assertThat(contents.get(0).uploaderId()).isEqualTo(userId);
        assertThat(contents.get(0).status()).isEqualTo(ContentStatus.UPLOADING);
    }

    @Test
    void upload_success(){
        MultipartFile file = new MockMultipartFile(
                "test", "test.png", "image/png", new byte[1024]
        );
        Long userId = 10L;
        Long postId = 1L;

        given(minioService.uploadImage(file))
                .willReturn("contents/images/uuid-test.png");

        contentService.uploadContent(file, postId, userId);

        verify(minioService).uploadImage(file);
        verify(contentRepository).save(any(Content.class));
    }

    @Test
    void upload_fail_when_invalid_type(){
        MultipartFile file = new MockMultipartFile(
                "test", "test.exe", "application/octet-stream", new byte[10]
        );

        assertThatThrownBy(()->
            contentService.uploadContent(file,10L, 1L)
        ).isInstanceOf(ContentException.class);

        verifyNoInteractions(minioService);
        verify(contentRepository, never()).save(any());
    }

    @Test
    void upload_fail_when_minio_error(){
        MultipartFile file = new MockMultipartFile(
                "test", "test.png", "image/png", new byte[1024]
        );

        given(minioService.uploadImage(file))
                .willThrow(new MinioException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "업로드 실패"
                ));

        assertThatThrownBy(()->
            contentService.uploadContent(file,10L, 1L)
        ).isInstanceOf(MinioException.class);

        verify(contentRepository,never()).save(any());
    }

    @Test
    void download_success(){
        Long contentId = 20L;

        Content content = Content.builder()
                .postId(1L)
                .uploaderId(10L)
                .fileName("test.png")
                .objectKey("contents/images/test.png")
                .fileSize(1024L)
                .contentType("png")
                .build();
        content.changeAvailable();

        given(contentRepository.findById(contentId))
                .willReturn(Optional.of(content));
        given(postRepository.findById(1L))
                .willReturn(Optional.of(new Post()));
        given(minioService.generateDownloadUrl(content.getObjectKey()))
                .willReturn("http://minio/download-url");

        DownloadUrlResponse response = contentService.getDownloadUrl(contentId);

        assertThat(response.downloadUrl()).isEqualTo("http://minio/download-url");
        verify(minioService).generateDownloadUrl(content.getObjectKey());
    }

    @Test
    void download_fail_when_content_not_found(){
        given(contentRepository.findById(1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(()->
            contentService.getDownloadUrl(1L)
        ).isInstanceOf(ContentException.class).hasMessage("파일 없음");

        verifyNoInteractions(minioService);
        verifyNoInteractions(postRepository);
    }

    @Test
    void download_fail_when_content_not_available(){
        Content content = Content.builder().build();

        given(contentRepository.findById(1L))
                .willReturn(Optional.of(content));

        assertThatThrownBy(()->
            contentService.getDownloadUrl(1L)
        ).isInstanceOf(ContentException.class).hasMessage("컨텐츠에 접근할 수 없습니다.");

        verifyNoInteractions(minioService);
        verifyNoInteractions(postRepository);
    }

    @Test
    void download_fail_when_invalid_post(){
        Long postId = 1L;
        Content content = Content.builder()
                .postId(postId)
                .build();
        content.changeAvailable();

        given(contentRepository.findById(10L))
                .willReturn(Optional.of(content));

        given(postRepository.findById(postId))
                .willReturn(Optional.empty());

        assertThatThrownBy(()->
            contentService.getDownloadUrl(10L)
        ).isInstanceOf(PostException.class).hasMessage("게시물 없음");

        verifyNoInteractions(minioService);
    }
}
