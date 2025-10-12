package com.mik.openfegin.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public Retryer retryer() {
        // period=100 发起当前请求的时间间隔,单位毫秒
        // maxPeriod=1000 发起当前请求的最大时间间隔,单位毫秒
        // maxAttempts=2 重试次数是1，因为包括第一次，所以我们如果想要重试2次，就需要设置为3
        Retryer retryer = new Retryer.Default(1000, 1000, 3);
        return retryer;
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(5000, 10000); // 连接超时5秒，读超时10秒
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public okhttp3.OkHttpClient.Builder okHttpClientBuilder() {
        return new okhttp3.OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS);
//                .addInterceptor(new LoggingInterceptor());
    }

    /**
     * okhttp3 请求日志拦截器
     */
    static class LoggingInterceptor implements Interceptor {

        @NotNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            okhttp3.Request request = chain.request();
            long start = System.nanoTime();
            log.info(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));
            Response response = chain.proceed(request);
            long end = System.nanoTime();
            log.info(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (end - start) / 1e6d, response.headers()));
            return response;
        }
    }

}
