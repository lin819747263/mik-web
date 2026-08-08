package com.mik.user.controller;

import com.mik.core.pojo.Result;
import com.mik.redis.service.LoginFailureService;
import com.mik.security.captcha.CaptchaVerifier;
import com.mik.user.captcha.SimpleCaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController implements CaptchaVerifier {

    @Autowired
    private LoginFailureService loginFailureService;

    @Autowired
    private SimpleCaptchaService simpleCaptchaService;

    /**
     * 获取验证码
     */
    @GetMapping("/get")
    public Result<Map<String, Object>> getCaptcha() {
        SimpleCaptchaService.CaptchaResult result = simpleCaptchaService.generateCaptcha();
        Map<String, Object> data = new HashMap<>();
        data.put("captchaId", result.getCaptchaId());
        data.put("image", result.getImage());
        return Result.success(data);
    }

    /**
     * 校验验证码
     */
    @PostMapping("/check")
    public Result<Map<String, Object>> checkCaptcha(@RequestParam String captchaId,
                                                     @RequestBody Map<String, Object> body) {
        String code = body.get("code") != null ? body.get("code").toString() : "";
        boolean verified = simpleCaptchaService.verifyCaptcha(captchaId, code);

        Map<String, Object> data = new HashMap<>();
        data.put("verified", verified);
        if (verified) {
            data.put("token", captchaId);
        }
        return Result.success(data);
    }

    /**
     * 检查是否需要验证码（登录失败次数 >= 3）
     */
    @GetMapping("/need")
    public Result<Map<String, Object>> needCaptchaApi(@RequestParam String username) {
        boolean need = needCaptcha(username);
        Integer failCount = loginFailureService.getFailCount(username);
        Map<String, Object> data = new HashMap<>();
        data.put("need", need);
        data.put("failCount", failCount != null ? failCount : 0);
        return Result.success(data);
    }

    /**
     * 实现 CaptchaVerifier 接口：检查是否需要验证码
     */
    @Override
    public boolean needCaptcha(String username) {
        return loginFailureService.needCaptcha(username);
    }

    /**
     * 实现 CaptchaVerifier 接口：验证 captchaId 是否已通过验证
     */
    @Override
    public boolean isCaptchaVerified(String captchaId) {
        return simpleCaptchaService.isVerified(captchaId);
    }
}
