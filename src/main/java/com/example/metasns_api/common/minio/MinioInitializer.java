package com.example.metasns_api.common.minio;

import com.example.metasns_api.common.exception.MinioException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioInitializer {

    private final MinioClient minioClient;

    @Value("${storage.bucket}")
    private String BUCKET_NAME;

   @PostConstruct
    public void init(){
       try{
           boolean isExist = minioClient.bucketExists(
                   BucketExistsArgs.builder()
                           .bucket(BUCKET_NAME)
                           .build()
           );

           if(!isExist){
               minioClient.makeBucket(MakeBucketArgs.builder()
                       .bucket(BUCKET_NAME)
                       .build());
           }
       } catch (Exception e){
           throw new MinioException(
                   HttpStatus.INTERNAL_SERVER_ERROR,
                   "MinIO bucket 초기화 실패",
                   e
                   );
       }
   }
}
