package com.mik.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UsernamePasswordToken extends AbstractAuthenticationToken {

    private String principal;

    private String credentials;

    public UsernamePasswordToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public UsernamePasswordToken(Collection<? extends GrantedAuthority> authorities, String principal, String credentials) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
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
