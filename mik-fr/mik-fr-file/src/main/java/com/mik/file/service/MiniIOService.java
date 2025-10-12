package com.mik.file.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

@Configuration
@ConditionalOnProperty(name = "file.type", havingValue = "miniIO")
public class MiniIOService{

    @Autowired
    MinioClient minioClient;

    public void upload(String bucket, String fileName, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }catch (Exception e){
            throw new RuntimeException("文件上传失败");
        }

    }

}
