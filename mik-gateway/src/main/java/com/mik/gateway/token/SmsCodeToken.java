package com.mik.gateway.token;

import com.mik.gateway.service.LoginUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SmsCodeToken extends AbstractAuthenticationToken {

    private String mobile;
    private String code;
    private LoginUser principal;

    public SmsCodeToken(Collection<? extends GrantedAuthority> authorities, String mobile, String code) {
        super(authorities);
        this.mobile = mobile;
        this.code = code;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setPrincipal(LoginUser principal) {
        this.principal = principal;
    }

    @Override
    public Object getCredentials() {
        return mobile;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
