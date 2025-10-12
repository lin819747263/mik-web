package com.mik.gateway.handler;

import com.alibaba.fastjson.JSONObject;
import com.mik.core.constant.ResultCode;
import com.mik.core.pojo.Result;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class DefaultFailHandler implements ServerAuthenticationFailureHandler {

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        Result result = new Result();
        if (exception instanceof UsernameNotFoundException) {
            // 用户不存在
            result.setCode(ResultCode.SUCCESS.getCode());
            result.setMsg(exception.getMessage());
        } else if (exception instanceof BadCredentialsException) {
            // 密码错误
            result.setCode(ResultCode.SUCCESS.getCode());
            result.setMsg(exception.getMessage());
        } else if (exception instanceof LockedException) {
            // 用户被锁
            result.setCode(ResultCode.SUCCESS.getCode());
            result.setMsg(exception.getMessage());
        } else {
            // 系统错误
            result.setCode(ResultCode.SERVER_ERROR.getCode());
            result.setMsg(exception.getMessage());
        }
        String body = JSONObject.toJSONString(result);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
