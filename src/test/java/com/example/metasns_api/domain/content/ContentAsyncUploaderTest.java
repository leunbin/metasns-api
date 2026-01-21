package com.example.metasns_api.domain.content;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class ContentAsyncUploaderTest {

    @Mock
    ContentRepository contentRepository;

    @Mock
    MinioService minioService;

    @InjectMocks
    ContentAsyncUploader contentAsyncUploader;

    @Test
    void upload_success(){
        Content  content = Content.builder().build();
        ReflectionTestUtils.setField(content, "id",1L);

        given(contentRepository.findById(1L))
                .willReturn(Optional.of(content));
        given(minioService.uploadImage(any()))
                .willReturn("object-key");

        contentAsyncUploader.upload(1L, mock(MultipartFile.class));

        assertThat(content.getStatus()).isEqualTo(ContentStatus.AVAILABLE);
    }

    @Test
    void upload_fail(){
        Content content = Content.builder().build();
        ReflectionTestUtils.setField(content, "id", 1L);

        given(contentRepository.findById(1L))
                .willReturn(Optional.of(content));
        given(minioService.uploadImage(any()))
                .willThrow(new RuntimeException());

        contentAsyncUploader.upload(1L, mock(MultipartFile.class));

        assertThat(content.getStatus()).isEqualTo(ContentStatus.FAILED);
    }
}
