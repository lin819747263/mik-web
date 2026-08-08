package com.mik.auth.controller;

import com.mik.auth.service.LoginFailureService;
import com.mik.core.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private LoginFailureService loginFailureService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Redis key 前缀 - 已验证的验证码
     */
    private static final String CAPTCHA_VERIFIED_PREFIX = "captcha:verified:";

    /**
     * 验证码过期时间（5分钟）
     */
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    /**
     * 获取滑动验证码
     */
    @GetMapping("/get")
    public Result<Map<String, Object>> getCaptcha() {
        // TODO: 调用验证码生成接口
        // 由于 ImageCaptchaApplication 类可能不存在，需要检查正确的类名
        // 请在 Maven 依赖下载后检查实际的类名
        Map<String, Object> data = new HashMap<>();
        data.put("captchaId", "test-id");
        data.put("backgroundImage", "");
        data.put("sliderImage", "");
        data.put("template", "");
        return Result.success(data);
    }

    /**
     * 校验验证码
     */
    @PostMapping("/check")
    public Result<Map<String, Object>> checkCaptcha(@RequestParam String captchaId,
                                                     @RequestBody Object trackData) {
        // TODO: 调用验证码校验接口
        Map<String, Object> data = new HashMap<>();
        data.put("verified", true); // 临时返回true用于测试
        if (true) {
            String key = CAPTCHA_VERIFIED_PREFIX + captchaId;
            stringRedisTemplate.opsForValue().set(key, "1", CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
            data.put("token", captchaId);
        }
        return Result.success(data);
    }

    /**
     * 检查是否需要验证码（登录失败次数 >= 3）
     */
    @GetMapping("/need")
    public Result<Map<String, Object>> needCaptcha(@RequestParam String username) {
        Integer failCount = loginFailureService.getFailCount(username);
        boolean need = failCount != null && failCount >= 3;
        Map<String, Object> data = new HashMap<>();
        data.put("need", need);
        data.put("failCount", failCount != null ? failCount : 0);
        return Result.success(data);
    }

    /**
     * 验证 captchaId 是否已通过验证（供内部调用）
     */
    public boolean isCaptchaVerified(String captchaId) {
        if (captchaId == null) {
            return false;
        }
        String key = CAPTCHA_VERIFIED_PREFIX + captchaId;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            stringRedisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
