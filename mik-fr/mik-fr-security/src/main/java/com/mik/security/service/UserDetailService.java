package com.mik.security.service;

import com.mik.core.user.UserAuthService;
import com.mik.security.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    UserAuthService userService;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.mik.core.user.UserInfo dto;
        try {
            dto = userService.getUserByIdentify(username);
        }catch (Exception e){
            throw new UsernameNotFoundException(e.getMessage());
        }

        if (dto == null || dto.getUsername() == null) {
            return null;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(dto.getUserId());
        userInfo.setUsername(dto.getUsername());
        userInfo.setPassword(dto.getPassword());
        userInfo.setEnabled(dto.getEnable() == 1);
        return userInfo;
    }
}
