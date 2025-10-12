package com.mik.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author linguoshuai
 * @since 2019/4/16
 */
@Component
@ConfigurationProperties(prefix = "file-path")
public class StaticResourceConfigure {
    private String path;
    private String url;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
