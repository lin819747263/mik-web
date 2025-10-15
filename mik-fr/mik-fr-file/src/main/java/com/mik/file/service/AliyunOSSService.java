package com.mik.file.service;

import com.mik.file.FileOutput;
import com.mik.file.OSSUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Configuration
@ConditionalOnProperty(name = "file.type", havingValue = "aliyun")
public class AliyunOSSService implements FileService{

    public void upload(String bucket, String fileName, InputStream inputStream) {
        OSSUtil.uploadByInputStream(OSSUtil.getOSSClient(), inputStream, bucket, fileName);
    }

    @Override
    public FileOutput upload(MultipartFile file, String path, Boolean rename) {
        return null;
    }
}
