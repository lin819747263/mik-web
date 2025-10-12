package com.mik.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    private static OssProperties properties;

    public static OssProperties getInstance() {
        return properties;
    }

    public OssProperties() {
        properties = this;
    }

    private String accessKey;
    private String accessSecret;
    private String endPoint;
    private String bucket;

    public static String getUrl(){
        return "http://" + OssProperties.getInstance().getBucket() + "." +  OssProperties.getInstance().getEndPoint() + "/";
    }
}
