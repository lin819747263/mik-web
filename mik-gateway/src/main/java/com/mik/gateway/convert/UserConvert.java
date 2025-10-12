package com.mik.gateway.convert;

import com.mik.gateway.token.SmsCodeToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerFormLoginAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Component
public class UserConvert extends ServerFormLoginAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        Mono<MultiValueMap<String, String>> map = exchange.getFormData();
        return map.map(this::createAuthentication);
    }

    private SmsCodeToken createAuthentication(MultiValueMap<String, String> data) {
        String mobile = data.getFirst("mobile");
        String code = data.getFirst("code");
        SmsCodeToken token = new SmsCodeToken(new ArrayList<>(), mobile, code);

        return token;
    }
}
