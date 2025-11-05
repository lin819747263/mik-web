package com.mik.sys;

import com.mik.core.pojo.Result;
import com.mik.core.util.ObjectMapper;
import com.mik.security.UserContext;
import com.mik.sys.entity.OperationLogEntity;
import com.mik.sys.service.OperationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationService operationService;

    @Around("@annotation(com.mik.sys.OperationLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        OperationLog anno = AnnotationUtils.findAnnotation(method, OperationLog.class);

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs == null ? null :
                (HttpServletRequest) attrs.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        // 基础信息
        String requestUrL = request != null ? request.getRequestURL().toString() : "";
        String clientIp = getClientIp(request);

//        String requestParams = null;
//        if (anno != null && anno.saveRequest()) {
//            requestParams = LogUtils.sanitizeParams(pjp.getArgs());
//        }

        Object result = null;
        Throwable ex = null;
        int res = 0;

        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable e) {
            res = 1;
            ex = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;

            if (result instanceof Throwable) {
                res = 1;
            }

            if (result instanceof Result) {
                int code = ((Result) result).getCode();
                res = code == 0 ?  0 :  1;
            }

            OperationLogEntity record = OperationLogEntity.builder()
                    .url(requestUrL)
                    .param(getParameter(pjp, anno.paramRecord()))
                    .userId(UserContext.getUserId())
                    .operationName(anno.operation())
                    .ip(clientIp)
                    .result(res)
                    .build();

            // 持久化
            try {
                operationService.save(record);
            } catch (Exception saveEx) {
                log.warn("OperationLog persist failed: {}", saveEx.getMessage(), saveEx);
                // 降级打印
                log.info("OP_LOG => {}", record);
            }
        }
    }

    public String getParameter(ProceedingJoinPoint pjp, boolean param) {
        if (!param) {
            return "";
        }
        // 获取方法签名
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        // 获取方法参数名
        String[] parameterNames = signature.getParameterNames();
        // 获取方法参数值
        Object[] parameterValues = pjp.getArgs();

        // 构建参数Map
        Map<String, Object> paramMap = new HashMap<>();
        if (parameterNames != null && parameterValues != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                paramMap.put(parameterNames[i], parameterValues[i]);
            }
        }

        // 转换为JSON
        String paramsJson = "";
        try {
            paramsJson = ObjectMapper.getObjectMapper().writeValueAsString(paramMap);
        } catch (Exception e) {
            log.error("参数转JSON失败", e);
            paramsJson = "参数序列化失败: " + e.getMessage();
        }
        return paramsJson;
    }

    private String abbreviate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private String getClientIp(HttpServletRequest request) {
        String[] ipHeaders = {
                "X-Real-IP",
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        // 遍历检测IP头信息
        for (String header : ipHeaders) {
            String ip = getValidIp(request.getHeader(header));
            if (ip != null) {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    private String getValidIp(String ip) {
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            return null;
        }

        // 处理X-Forwarded-For情况
        if (ip.contains(",")) {
            String[] ips = ip.split(",");
            for (String candidateIp : ips) {
                String trimmedIp = candidateIp.trim();
                if (!isInvalidIp(trimmedIp)) {
                    return trimmedIp;
                }
            }
            return null;
        }

        return ip.trim();
    }

    /**
     * 判断是否为无效IP
     */
    private boolean isInvalidIp(String ip) {
        return StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip);
    }
}
