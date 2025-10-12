package com.mik.user.api.fallback;

import com.mik.core.pojo.Result;
import com.mik.core.user.UserInfo;
import com.mik.user.api.dto.UserDTO;
import com.mik.user.api.user.UserRpc;

//@Component
public class UserFallBack implements UserRpc {

    @Override
    public Result getUserById(Long userId) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("默认数据");
        userDTO.setEmail("666@qq.com");
        return Result.success(userDTO);
    }

    @Override
    public UserInfo getUserByIdentify(String identify) {
        return new UserInfo();
    }
}
