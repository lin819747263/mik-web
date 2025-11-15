package com.mik.mvc.fliter;

import com.google.common.util.concurrent.RateLimiter;
import com.mik.core.pojo.Result;
import com.mik.core.util.HttpServletUtil;
import com.mik.exception.CommonErrorCode;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class RateLimitFilter implements Filter {

    @Autowired
    private RedisRateLimiter redisRateLimiter;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String ip = getClientIp(req);
        String key = "rate_limit:" + ip;

        if (!redisRateLimiter.isAllowed(key, 500, 10)) { // 1分钟内最多5次
            Result<Void> error = Result.error(CommonErrorCode.TooManyRequests);
            HttpServletUtil.writeData((HttpServletResponse)response, error);
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
