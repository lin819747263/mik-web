package com.mik.user.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserCreateDTO {
    private Long userId;
    private String username = "";
    private String nickname = "";
    private String mobile = "";
    private String email = "";
    private String password;
    private String avatar = "";
    private Integer sex = 1;
    private Integer enable = 1;
    private Date birthday = new Date();
    private String roleIds;
}
