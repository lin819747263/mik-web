package com.mik.security.provider;

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

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authentication;
        String username = token.getPrincipal().toString();
        String password = token.getCredentials().toString();

        UserInfo userInfo = (UserInfo) userDetailService.loadUserByUsername(username);

        if (userInfo == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            throw new UsernameNotFoundException("用户名或者密码错误");
        }
        if(!encoder.matches(password, userInfo.getPassword())){
            throw new BadCredentialsException("用户名或者密码错误");
        }
        token.setDetails(userInfo);

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordToken.class);
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123456"));
    }
}
