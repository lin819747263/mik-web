package com.mik.gateway.manager;

import com.mik.gateway.service.LoginUser;
import com.mik.gateway.service.UserDetailService;
import com.mik.gateway.token.SmsCodeToken;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractUserDetailsReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;

//@Component
public class SmsCodeAuthenticationManager extends AbstractUserDetailsReactiveAuthenticationManager {

//    @Resource
    UserDetailService userDetailService;

    public SmsCodeAuthenticationManager(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!SmsCodeToken.class.isAssignableFrom(authentication.getClass())){
            return Mono.just(authentication);
        }
        SmsCodeToken smsCodeToken = (SmsCodeToken)authentication;
        String username = smsCodeToken.getMobile();
        String code = smsCodeToken.getCode();
        if(StringUtils.isBlank(username)){
            throw new UsernameNotFoundException("用户名不存在");
        }
        if(!"666666".equals(code)){
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        LoginUser userDetails = (LoginUser)retrieveUser(username).block();
        smsCodeToken.setPrincipal(userDetails);
        return Mono.just(smsCodeToken);
    }

    @Override
    protected Mono<UserDetails> retrieveUser(String username) {
        return userDetailService.findByUsername(username);
    }
}
