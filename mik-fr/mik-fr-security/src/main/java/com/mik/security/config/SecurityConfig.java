package com.mik.security.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mik.client.WhiteListProperties;
import com.mik.client.filter.AuthorFilter;
import com.mik.core.constant.CommonConstant;
import com.mik.core.pojo.Result;
import com.mik.core.util.HttpServletUtil;
import com.mik.core.util.ObjectMapper;
import com.mik.exception.SecurityConstant;
import com.mik.security.UserInfo;
import com.mik.security.filter.SmsLoginFilter;
import com.mik.security.filter.UsernamePasswordFilter;
import com.mik.security.provider.SmsProvider;
import com.mik.security.provider.UsernameAndPasswordProvider;
import com.mik.security.service.UserDetailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.DigestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    RedisTemplate redisTemplate;


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
                            .requestMatchers(whiteListProperties.getUrls()).permitAll()
//                            .requestMatchers("/doc.html", "/v3/api-docs", "/swagger-resources/**",
//                                    "/webjars/**", "/configuration/ui", "/configuration/security",
//                                    "/favicon.ico", "/css/**", "/js/**", "/img/**", "/fonts/**","/v3/api-docs"
//                            , "/.well-known/appspecific/com.chrome.devtools.json")
//                            .permitAll()
//
//                            .requestMatchers("/login","/sms/login","/v3/api-docs/swagger-config").permitAll()
                            .anyRequest().authenticated();
                }).csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(new AuthorFilter(whiteListProperties), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(smsAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(usernamePasswordFilter1, UsernamePasswordAuthenticationFilter.class);

        return httpConfig.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(whiteListProperties.getOrigins())); // 允许的源
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允许的HTTP方法
        configuration.setAllowedHeaders(Arrays.asList("*")); // 允许的头
        configuration.setAllowCredentials(true); // 是否允许携带cookie
        configuration.setMaxAge(3600L); // 预检请求的有效期，单位为秒

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 对所有路径应用这个配置
        return source;
    }


    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            JSONObject jsonObject = new JSONObject();
            UserInfo details = (UserInfo) authentication.getDetails();
            long random = System.currentTimeMillis() + new Random(100).nextInt();
            String hash = DigestUtils.md5DigestAsHex(Long.toString(random).getBytes(StandardCharsets.UTF_8));

            String token = sign(hash, details.getUserId().toString());
            jsonObject.put(CommonConstant.AUTH_HEADER, token);
            redisTemplate.opsForValue().set(StrUtil.format("Auth:{}:{}",details.getUserId(), hash), token , 3, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(StrUtil.format("info:{}",details.getUserId()), JSON.toJSONString(details));
            HttpServletUtil.writeData(response, Result.success(jsonObject));
        };
    }

    // 认证失败处理器
    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            Result r = Result.error(SecurityConstant.USERNAME_PASSWORD_ERROR);
            HttpServletUtil.writeData(response, r);
        };
    }


    public String sign(String hash,String userId){
        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", userId);
        claims.put("iat", new Date());
        claims.put("hash", hash);

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
