package com.mik.client.filter;


import com.mik.client.UserInfoToken;
import com.mik.client.WhiteListProperties;
import com.mik.core.constant.CommonConstant;
import com.mik.core.pojo.Result;
import com.mik.core.util.HttpServletUtil;
import com.mik.exception.SecurityConstant;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class AuthorFilter extends OncePerRequestFilter {

    private AntPathMatcher matcher = new AntPathMatcher();

    private String[] whiteList;

//    = {
//            "/sms/login",
//            "/login",
//            "/doc.html", // Knife4j UI入口
//            "/v3/api-docs",
//            "/v3/api-docs/**", // Swagger的JSON API文档路径
//            "/v3/api-docs/swagger-config",
//            "/swagger-resources/", // Swagger的资源文件路径
//            "/webjars/", // Swagger UI和Knife4j所依赖的WebJars资源路径
//            "/configuration/ui", // Swagger UI配置路径
//            "/configuration/security", // Swagger安全配置路径
//            "/favicon.ico", // 网站图标
//            "/css/**", "/js/**", "/img/**", "/fonts/**",
//            "/.well-known/appspecific/com.chrome.devtools.json"// 静态资源路径
//    };

    public AuthorFilter(WhiteListProperties properties){
        this.whiteList = properties.getUrls();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println(request.getRequestURI());
        if(isWhiteListPath(request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(CommonConstant.AUTH_HEADER);
        if(StringUtils.isBlank(authHeader)){
            unAuth(response);
            return;
        }

        try {
            String secretKey = Base64.getEncoder().encodeToString("mik".getBytes(StandardCharsets.UTF_8)) ;
            String username = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(authHeader)
                    .getBody()
                    .getSubject();


            String tokenId = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(authHeader)
                    .getBody()
                    .getId();


            UserInfoToken token = new UserInfoToken(new ArrayList<>(), username, tokenId);
            token.setAuthenticated(true);
            token.setDetails(null);
            SecurityContextHolder.getContext().setAuthentication(token);
        }catch (Exception e){
            HttpServletUtil.writeData(response, Result.error(SecurityConstant.AUTH_ERROR));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void unAuth(HttpServletResponse response) throws IOException {
        HttpServletUtil.writeData(response, Result.error(SecurityConstant.AUTH_ERROR));
    }

    private boolean isWhiteListPath(String path) {
        for (String whiteListPath : whiteList) {
            if (matcher.match(whiteListPath, path)) {
                return true;
            }
        }
        return false;
    }
}
