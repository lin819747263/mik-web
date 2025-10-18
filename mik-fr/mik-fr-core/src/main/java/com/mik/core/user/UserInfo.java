package com.mik.core.user;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class UserInfo {
    private Long userId;
    private String username;
    private String nickname;
    private String mobile;
    private String email;
    private String password ;
    private String avatar;
    private Integer sex;
    private Date birthday;
    private Date createTime;
    private Integer enable;
}
