package com.example.metasns_api.domain.content;

import com.example.metasns_api.common.exception.MinioException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${storage.bucket}")
    private String BUCKET_NAME;

    @Value("${storage.path}")
    private String PATH;

    @Value("${storage.image-dir}")
    private String IMAGE_DIR;

    private String generateImageObjectKey(String originalFilename){
        return PATH + "/" + IMAGE_DIR + "/" + UUID.randomUUID() + "-" + originalFilename;
    }

    public String uploadImage(MultipartFile file){
        try{
            String objectKey = generateImageObjectKey(file.getOriginalFilename());

            minioClient.putObject(PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return objectKey;
        } catch (Exception e){
            throw new MinioException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "업로드 실패",
                    e
            );
        }
    }

    public String generateDownloadUrl(String objectKey){
        try{
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(BUCKET_NAME)
                            .object(objectKey)
                            .expiry(5, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e){
            throw new MinioException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "다운로드 URL 생성 실패",
                    e
            );
        }
    }
}
