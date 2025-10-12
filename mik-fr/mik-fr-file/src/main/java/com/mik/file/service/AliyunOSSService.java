package com.mik.file.service;

import com.mik.file.OSSUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
@ConditionalOnProperty(name = "file.type", havingValue = "aliyun")
public class AliyunOSSService{

    public void upload(String bucket, String fileName, InputStream inputStream) {
        OSSUtil.uploadByInputStream(OSSUtil.getOSSClient(), inputStream, bucket, fileName);
    }
}
