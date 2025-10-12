package com.mik.gateway.service;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Data
public class LoginUser implements UserDetails {

    /**
     * token
     */
    private String token;

    /**
     * login time
     */
    private Long loginTime;

    /**
     * expire time
     */
    private Long expireTime;

    /**
     * Login IP address
     */
    private String ip;

    /**
     * location
     */
    private String location;

    /**
     * Browser type
     */
    private String browser;

    /**
     * operating system
     */
    private String os;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 账号密码
     */
    private String userPwd;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
