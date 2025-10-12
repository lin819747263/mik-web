package com.mik.client;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserInfoToken extends AbstractAuthenticationToken {

    private String mobile;
    private String code;

    public UserInfoToken(Collection<? extends GrantedAuthority> authorities, String mobile, String code) {
        super(authorities);
        this.mobile = mobile;
        this.code = code;
    }


    @Override
    public Object getCredentials() {
        return mobile;
    }

    @Override
    public Object getPrincipal() {
        return code;
    }
}
