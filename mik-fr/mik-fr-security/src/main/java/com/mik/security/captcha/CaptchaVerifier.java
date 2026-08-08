package com.mik.security.captcha;

/**
 * 验证码验证接口
 * 由具体模块实现（如 mik-user 中的 CaptchaController）
 */
public interface CaptchaVerifier {

    /**
     * 检查是否需要验证码
     * @param username 用户名
     * @return true=需要验证码
     */
    boolean needCaptcha(String username);

    /**
     * 验证 captchaId 是否已通过验证
     * @param captchaId 验证码ID
     * @return true=验证通过
     */
    boolean isCaptchaVerified(String captchaId);
}
