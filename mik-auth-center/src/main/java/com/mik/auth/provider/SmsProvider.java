package com.mik.auth.provider;

import com.mik.auth.UserInfo;
import com.mik.auth.service.UserDetailService;
import com.mik.auth.token.SmsToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SmsProvider implements AuthenticationProvider {

    @Autowired
    UserDetailService userDetailService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsToken token = (SmsToken) authentication;
        String mobile = token.getPrincipal().toString();
        String code = token.getCredentials().toString();

        UserInfo userInfo = (UserInfo) userDetailService.loadUserByUsername(mobile);

        if (userInfo == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        if(StringUtils.isBlank(mobile) || StringUtils.isBlank(code)){
            throw new UsernameNotFoundException("用户名或者密码不存在");
        }
        if(!"666888".equals(code)){
            throw new BadCredentialsException("用户名或者密码错误");
        }
        token.setDetails(userInfo);

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsToken.class.isAssignableFrom(authentication);
    }
}
