package com.example.metasns_api.domain.content;

import com.example.metasns_api.common.exception.ContentException;
import com.example.metasns_api.common.exception.PostException;
import com.example.metasns_api.domain.content.dto.ContentFileResponse;
import com.example.metasns_api.domain.content.dto.ContentFileStatusResponse;
import com.example.metasns_api.domain.content.dto.DownloadUrlResponse;
import com.example.metasns_api.domain.post.Post;
import com.example.metasns_api.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentService {
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/png",
            "image/jpeg",
            "application/pdf"
    );

    private final ContentRepository contentRepository;

    private final PostRepository postRepository;

    private final MinioService minioService;

    private final ContentAsyncUploader contentAsyncUploader;

    private void validate(MultipartFile file){
        System.out.println("contentType: "+ file.getContentType() + "size: "+file.getSize());
        if(file.isEmpty()){
            throw new ContentException(
                    HttpStatus.NO_CONTENT,
                    "업로드 될 파일이 없습니다."
            );
        }

        if(file.getSize() > MAX_IMAGE_SIZE){
            throw new ContentException(
                    HttpStatus.CONTENT_TOO_LARGE,
                    "파일 최대 5MB까지 업로드 가능합니다."
            );
        }

        if(!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ContentException(
                    HttpStatus.EXPECTATION_FAILED,
                    "허용되지 않는 타입입니다."
            );
        }
    }

    public List<ContentFileResponse> getContentsByPostId(Long postId){
        ContentStatus available = ContentStatus.AVAILABLE;
        List<Content> contents = contentRepository.findByPostIdAndStatus(postId, available);

        return contents
                .stream()
                .map(content -> new ContentFileResponse(
                        content.getUploaderId(),
                        content.getFileName(),
                        content.getObjectKey(),
                        content.getFileSize(),
                        content.getContentType()
                ))
                .toList();
    }

    public List<ContentFileStatusResponse> getContentsInUploading(Long uploaderId){
        ContentStatus uploading = ContentStatus.UPLOADING;
        List<Content> contents = contentRepository.findByUploaderIdAndStatus(uploaderId, uploading);



        return contents
                .stream()
                .map(content -> new ContentFileStatusResponse(
                        content.getPostId(),
                        content.getUploaderId(),
                        content.getFileName(),
                        content.getObjectKey(),
                        content.getFileSize(),
                        content.getContentType(),
                        content.getStatus()
                ))
                .toList();
    }

    //일단 업로딩중으로 요청 보냄
    public void requestUpload(MultipartFile file, Long postId, Long userId){
        validate(file);
        Content content = Content.builder()
                .postId(postId)
                .uploaderId(userId)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        contentRepository.save(content);

        contentAsyncUploader.upload(content.getId(), file);
    }

    public DownloadUrlResponse getDownloadUrl(Long contentId){
        Content content = contentRepository.findById(contentId)
                .orElseThrow(()->
                        new ContentException(
                                HttpStatus.NOT_FOUND,
                                "파일 없음"
                        ));

        if(content.getStatus() != ContentStatus.AVAILABLE){
            throw new ContentException(
                    HttpStatus.BAD_REQUEST,
                    "컨텐츠에 접근할 수 없습니다."
            );
        }

        postRepository.findById(content.getPostId())
                .orElseThrow(()->
                        new PostException(
                                HttpStatus.NOT_FOUND,
                                "게시물 없음"
                        ));

        String url = minioService.generateDownloadUrl(content.getObjectKey());

        return new DownloadUrlResponse(url);
    }
}
