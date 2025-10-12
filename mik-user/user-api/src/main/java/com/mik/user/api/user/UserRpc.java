package com.mik.user.api.user;

import com.mik.core.pojo.Result;
import com.mik.core.user.UserInfo;
import com.mik.openfegin.config.VersionBasedLoadBalancerConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "user-service", configuration = VersionBasedLoadBalancerConfiguration.class)
public interface UserRpc {

    @GetMapping("/user/getUserById")
    Result getUserById(@RequestParam(value = "userId") Long userId);

    @GetMapping("/user/getUserByIdentify")
    UserInfo getUserByIdentify(@RequestParam(value = "identify") String identify);

}
