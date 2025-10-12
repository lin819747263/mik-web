package com.mik.auth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SmsToken extends AbstractAuthenticationToken {
    private String principal;

    private String credentials;



    public SmsToken(Collection<? extends GrantedAuthority> authorities, String principal, String credentials) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
    }

    public SmsToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
