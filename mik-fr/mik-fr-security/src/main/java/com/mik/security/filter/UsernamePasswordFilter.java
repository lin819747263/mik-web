package com.mik.security.filter;

import com.alibaba.fastjson.JSON;
import com.mik.core.pojo.Result;
import com.mik.security.captcha.CaptchaVerifier;
import com.mik.security.token.UsernamePasswordToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.ArrayList;

public class UsernamePasswordFilter extends AbstractAuthenticationProcessingFilter {

    private CaptchaVerifier captchaVerifier;

    public UsernamePasswordFilter() {
        super("/login");
    }

    public void setCaptchaVerifier(CaptchaVerifier captchaVerifier) {
        this.captchaVerifier = captchaVerifier;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 检查是否需要验证码
        if (captchaVerifier != null && captchaVerifier.needCaptcha(username)) {
            String captchaId = request.getParameter("captchaId");

            // 验证码参数缺失
            if (captchaId == null || captchaId.isEmpty()) {
                writeError(response, 5002, "请输入验证码");
                return null;
            }

            // 检查验证码是否已验证通过
            if (!captchaVerifier.isCaptchaVerified(captchaId)) {
                writeError(response, 5002, "验证码无效或已过期，请重新验证");
                return null;
            }
        }

        UsernamePasswordToken token = new UsernamePasswordToken(new ArrayList<>(), username, password);
        return this.getAuthenticationManager().authenticate(token);
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(code, message);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
