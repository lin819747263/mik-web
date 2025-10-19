package com.mik.mvc.fliter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class RedisRateLimiter {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean isAllowed(String key, Integer maxPermits, Integer timeoutSeconds) {
        // 使用Lua脚本保证原子性
        String script = "local current = redis.call('INCR', KEYS[1]) " +
                "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                "return current <= tonumber(ARGV[2])";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Arrays.asList(key), timeoutSeconds.toString(), maxPermits.toString());
        return result != null && result <= maxPermits;
    }
}
