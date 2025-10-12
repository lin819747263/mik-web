package com.mik.gateway.config;

import com.mik.gateway.convert.UserConvert;
import com.mik.gateway.handler.DefaultDeniedHandler;
import com.mik.gateway.handler.DefaultFailHandler;
import com.mik.gateway.handler.DefaultSuccessHandler;
import com.mik.gateway.manager.SmsCodeAuthenticationManager;
import com.mik.gateway.manager.UsernamePassroedAuthenticationManager;
import com.mik.gateway.service.UserDetailService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;


@Slf4j
@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    @Resource
    DefaultFailHandler defaultFailHandler;
    @Resource
    DefaultSuccessHandler defaultSuccessHandler;
    @Resource
    UserDetailService userDetailService;
    @Resource
    DefaultDeniedHandler defaultDeniedHandler;
    @Resource
    UserConvert userConvert;

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .cors(corsSpec -> corsSpec.disable())
                .csrf(csrfSpec -> csrfSpec.disable())
                .httpBasic(httpBasicSpec -> httpBasicSpec.disable())
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec.accessDeniedHandler(defaultDeniedHandler))
                .authorizeExchange(exchanges -> {
                    exchanges.pathMatchers("/login").permitAll()
                            .anyExchange().permitAll();
                        }
                ).formLogin(formLoginSpec -> {
                    formLoginSpec
                            .authenticationManager(new UsernamePassroedAuthenticationManager(userDetailService))
                            .authenticationSuccessHandler(defaultSuccessHandler)
                            .authenticationFailureHandler(defaultFailHandler);
                });
        return http.build();
    }

    private AuthenticationWebFilter authenticationWebFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(new SmsCodeAuthenticationManager(userDetailService));

        filter.setServerAuthenticationConverter(userConvert);
        filter.setAuthenticationSuccessHandler(defaultSuccessHandler);
        filter.setAuthenticationFailureHandler(defaultFailHandler);
        filter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/sms/login")
        );

        return filter;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }



}
