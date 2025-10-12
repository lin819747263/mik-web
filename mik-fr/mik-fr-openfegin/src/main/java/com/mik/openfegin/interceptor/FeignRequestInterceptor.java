package com.mik.openfegin.interceptor;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

@Configuration
public class FeignRequestInterceptor implements RequestInterceptor{

    @Override
    public void apply(RequestTemplate template) {
        template.header("userId", getUserId().toString());
        template.header("version", getVersion());
    }

    private String getVersion() {
        return "0.0.1";
    }

    private Long getUserId(){
        return 0L;
    }
}
