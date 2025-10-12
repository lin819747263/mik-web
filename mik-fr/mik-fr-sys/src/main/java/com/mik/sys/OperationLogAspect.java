package com.mik.sys;

import com.mik.sys.entity.OperationLogEntity;
import com.mik.sys.service.OperationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;

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
        String className = pjp.getTarget().getClass().getName();
        String methodName = signature.getName();
        String requestUri = request != null ? request.getRequestURI() : null;
        String httpMethod = request != null ? request.getMethod() : null;

        String operator = "";
        String clientIp = "";

//        String requestParams = null;
//        if (anno != null && anno.saveRequest()) {
//            requestParams = LogUtils.sanitizeParams(pjp.getArgs());
//        }

        Object result = null;
        Throwable ex = null;

        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable e) {
            ex = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;

//            String responseStr = null;
//            if (anno != null && anno.saveResponse() && ex == null) {
//                responseStr = LogUtils.toJson(result);
//            }

            OperationLogEntity record = OperationLogEntity.builder()
                    .url("")
                    .param("")
                    .userId(1L)
                    .operationName("")
                    .ip("")
                    .result(1)
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

    private String abbreviate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
