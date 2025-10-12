package com.mik.gateway.service;

import com.mik.core.user.UserInfo;
import com.mik.user.api.dto.UserListDTO;
import com.mik.user.api.user.UserRpc;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class UserDetailService implements ReactiveUserDetailsService {

    @Autowired
    UserRpc userRpc;

    private ExecutorService service = Executors.newFixedThreadPool(1);

    @SneakyThrows
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        Future<UserInfo> future = service.submit(() -> userRpc.getUserByIdentify(username));
        UserInfo userListDTO =  future.get();
        LoginUser loginUser = new LoginUser();
        loginUser.setUserName(userListDTO.getUsername());
        loginUser.setUserPwd(userListDTO.getPassword());
        return Mono.just(loginUser);
    }
}
