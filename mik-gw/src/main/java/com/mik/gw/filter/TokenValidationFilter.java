package com.mik.gw.filter;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

//@Component
public class TokenValidationFilter implements GlobalFilter, Ordered {

    // 白名单路径
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/public",
            "/api/login",
            "/api/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 检查是否在白名单中
        if (isInWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 获取token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || token.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 验证token
        if (!isValidToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // token 有效，继续处理请求
        return chain.filter(exchange);
    }

    private boolean isInWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private boolean isValidToken(String token) {
        System.out.println("token为：" + token);
        // 在这里实现您的token验证逻辑
        // 例如，可以调用认证服务来验证token
        // 这里仅作为示例，始终返回true
        return true;
    }

    @Override
    public int getOrder() {
        // 设置过滤器顺序，数字越小优先级越高
        return Integer.MIN_VALUE;
    }
}

