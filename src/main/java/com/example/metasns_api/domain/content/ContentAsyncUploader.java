package com.example.metasns_api.domain.content;

import com.example.metasns_api.domain.content.dto.ContentUploadEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class ContentAsyncUploader {
    private final ContentRepository contentRepository;
    private final MinioService minioService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ContentUploadEvent event){
        Content content = contentRepository.findById(event.contentId())
                .orElseThrow(() -> new NoSuchElementException("Content not found"));
        try{
            String objectKey = minioService.uploadImage(
                    event.fileData(),
                    event.originalFilename(),
                    event.contentType()
            );

            content.updateObjectKey(objectKey);
            content.changeAvailable();
            contentRepository.save(content);
        } catch(Exception e){
            content.changeFailed();
            contentRepository.save(content);
        }
    }
}
