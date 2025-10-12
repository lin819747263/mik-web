package com.mik.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UserListDTO {
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
    private List<Long> roleIds = new ArrayList<>();
}
