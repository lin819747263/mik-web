package com.mik.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录失败次数记录服务 (Redis 版本)
 */
@Service
public class LoginFailureService {

    /**
     * Redis key 前缀
     */
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";

    /**
     * 失败次数过期时间（30分钟）
     */
    private static final long EXPIRE_MINUTES = 30;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 记录登录失败
     */
    public void recordFailure(String username) {
        String key = LOGIN_FAIL_PREFIX + username;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * 清除登录失败记录（登录成功时调用）
     */
    public void clearFailure(String username) {
        String key = LOGIN_FAIL_PREFIX + username;
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取失败次数
     */
    public Integer getFailCount(String username) {
        String key = LOGIN_FAIL_PREFIX + username;
        String count = stringRedisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    /**
     * 检查是否需要验证码
     */
    public boolean needCaptcha(String username) {
        Integer count = getFailCount(username);
        return count != null && count >= 3;
    }
}
