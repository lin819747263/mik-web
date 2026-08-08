package com.mik.security.provider;

import com.mik.redis.service.LoginFailureService;
import com.mik.security.UserInfo;
import com.mik.security.service.UserDetailService;
import com.mik.security.token.UsernamePasswordToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsernameAndPasswordProvider implements AuthenticationProvider {

    @Autowired
    UserDetailService userDetailService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired(required = false)
    LoginFailureService loginFailureService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authentication;
        if (token.getPrincipal() == null || token.getCredentials() == null) {
            throw new BadCredentialsException("用户名或密码不能为空");
        }
        String username = token.getPrincipal().toString();
        String password = token.getCredentials().toString();

        UserInfo userInfo = (UserInfo) userDetailService.loadUserByUsername(username);

        if (userInfo == null) {
            // 记录登录失败
            recordFailure(username);
            throw new UsernameNotFoundException("用户不存在");
        }
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            recordFailure(username);
            throw new UsernameNotFoundException("用户名或者密码错误");
        }
        if (!encoder.matches(password, userInfo.getPassword())) {
            // 记录登录失败
            recordFailure(username);
            throw new BadCredentialsException("用户名或者密码错误");
        }
        if (!userInfo.isEnabled()) {
            throw new UsernameNotFoundException("用户名被禁用");
        }

        // 登录成功，清除失败记录
        clearFailure(username);
        token.setDetails(userInfo);

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordToken.class);
    }

    /**
     * 记录登录失败
     */
    private void recordFailure(String username) {
        if (loginFailureService != null) {
            loginFailureService.recordFailure(username);
        }
    }

    /**
     * 清除登录失败记录
     */
    private void clearFailure(String username) {
        if (loginFailureService != null) {
            loginFailureService.clearFailure(username);
        }
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123456"));
    }
}
