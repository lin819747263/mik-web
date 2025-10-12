package com.mik.auth.service;

import com.mik.auth.UserInfo;
import com.mik.user.api.dto.UserListDTO;
import com.mik.user.api.user.UserRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    UserRpc userRpc;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.mik.core.user.UserInfo dto;
        try {
            dto = userRpc.getUserByIdentify(username);
        }catch (Exception e){
            throw new UsernameNotFoundException(e.getMessage());
        }

        if (dto == null || dto.getUsername() == null) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(dto.getUsername());
        userInfo.setPassword(dto.getPassword());
        return userInfo;
    }
}
