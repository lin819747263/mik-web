package com.mik.auth.config;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mik.auth.filter.SmsLoginFilter;
import com.mik.auth.filter.UsernamePasswordFilter;
import com.mik.auth.provider.SmsProvider;
import com.mik.auth.provider.UsernameAndPasswordProvider;
import com.mik.auth.service.UserDetailService;
import com.mik.client.WhiteListProperties;
import com.mik.client.filter.AuthorFilter;
import com.mik.core.constant.CommonConstant;
import com.mik.core.pojo.Result;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Autowired
    UserDetailService userDetailService;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    SmsProvider smsProvider;
    @Autowired
    UsernameAndPasswordProvider usernameAndPasswordProvider;

    @Autowired
    WhiteListProperties whiteListProperties;

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);

        builder.authenticationProvider(smsProvider);
        builder.authenticationProvider(usernameAndPasswordProvider);
        // 配置认证管理器
        builder.userDetailsService(userDetailService)
                .passwordEncoder(encoder);

        AuthenticationManager authenticationManager = builder.build();
//        设置认证管理器
        http.authenticationManager(authenticationManager);

        // 构建AuthenticationManager
        return authenticationManager;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        SmsLoginFilter smsAuthenticationFilter = new SmsLoginFilter();
        smsAuthenticationFilter.setAuthenticationManager(authenticationManager(http));
        smsAuthenticationFilter.setAuthenticationSuccessHandler(successHandler());
        smsAuthenticationFilter.setAuthenticationFailureHandler(failureHandler());

        UsernamePasswordFilter usernamePasswordFilter1 = new UsernamePasswordFilter();
        usernamePasswordFilter1.setAuthenticationManager(authenticationManager(http));
        usernamePasswordFilter1.setAuthenticationSuccessHandler(successHandler());
        usernamePasswordFilter1.setAuthenticationFailureHandler(failureHandler());


        HttpSecurity httpConfig = http.authorizeHttpRequests(requestMatcherRegistry -> {
                    requestMatcherRegistry
                            .requestMatchers("/doc.html", "/v3/api-docs", "/swagger-resources/**",
                                    "/webjars/**", "/configuration/ui", "/configuration/security",
                                    "/favicon.ico", "/css/**", "/js/**", "/img/**", "/fonts/**")
                            .permitAll()

                            .requestMatchers("/login","/sms/login").permitAll()
                            .anyRequest().authenticated();
                }).csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new AuthorFilter(whiteListProperties), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(smsAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(usernamePasswordFilter1, UsernamePasswordAuthenticationFilter.class)
                .cors(AbstractHttpConfigurer::disable);

        return httpConfig.build();
    }


    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            JSONObject jsonObject = new JSONObject();
            String mobile = authentication.getPrincipal().toString();
            jsonObject.put(CommonConstant.AUTH_HEADER, sign(mobile));
            Result r = Result.success(jsonObject);
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), r);
        };
    }

    // 认证失败处理器
    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            Result r = Result.error(5001, exception.getMessage());
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), r);
        };
    }


    public String sign(String username){
        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", username);
        claims.put("iat", new Date());

        // 设置签名的秘钥
        String secretKey = Base64.getEncoder().encodeToString("mik".getBytes(StandardCharsets.UTF_8)) ;

        // 设置过期时间，比如设置为1小时后
        long expirationTime = 60 * 60 * 1000; // 1 hour in milliseconds
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTime);

        // 构建JWT并进行签名
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

}
