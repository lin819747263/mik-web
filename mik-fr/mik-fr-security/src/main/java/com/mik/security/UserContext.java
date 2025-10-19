package com.mik.security;

import com.mik.core.user.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContext {

    public static com.mik.core.user.UserInfo getUserIfo(){
        try {
            return  (com.mik.core.user.UserInfo) SecurityContextHolder.getContext().getAuthentication().getDetails();
        }catch (Exception e){
            return new UserInfo().setUserId(0L).setUsername("匿名用户");
        }
    }

    public static Long getUserId(){
        return getUserIfo().getUserId();
    }
}
