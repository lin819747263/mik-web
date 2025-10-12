package com.mik.gateway.handler;

import com.alibaba.fastjson.JSONObject;
import com.mik.core.pojo.Result;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class DefaultSuccessHandler implements ServerAuthenticationSuccessHandler {

    @Autowired
    ReactiveRedisTemplate reactiveRedisTemplate;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.OK);

        String auth = Jwts.builder().setSubject("test").setId("666666").claim(authentication.getCredentials().toString(), authentication.getPrincipal())
                .signWith(SignatureAlgorithm.HS256, "test").compact();

        Mono<Boolean> res = reactiveRedisTemplate.opsForValue().set("auth" +  authentication.getCredentials(), auth);

        Result result = Result.success("登录成功");
        String body = JSONObject.toJSONString(result);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
