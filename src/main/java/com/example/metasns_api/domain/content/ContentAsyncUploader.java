package com.example.metasns_api.domain.content;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ContentAsyncUploader {
    private final ContentRepository contentRepository;
    private final MinioService minioService;

    @Async
    @Transactional
    public void upload(Long contentId, MultipartFile file){
        Content content = contentRepository.findById(contentId)
                .orElseThrow();
        try{
            String objectKey = minioService.uploadImage(file);

            content.updateObjectKey(objectKey);
            content.changeAvailable();
        } catch(Exception e){
            content.changeFailed();
        }
    }
}
