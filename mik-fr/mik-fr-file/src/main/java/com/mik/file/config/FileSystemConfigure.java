package com.mik.file.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author linguoshuai
 * @since 2019/4/16
 */
@Configuration
public class FileSystemConfigure implements WebMvcConfigurer {

    @Autowired
    private StaticResourceConfigure staticResourceConfigure;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + staticResourceConfigure.getPath());
        registry.addResourceHandler("/down/**").addResourceLocations("file:" + "/opt/static/");
    }

}
