package com.mik.user.entity;

import com.mik.db.entity.BaseDelEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("user")
public class User extends BaseDelEntity {
    @Id(keyType = KeyType.Auto)
    private Long userId;
    private String username;
    private String nickname;
    private String mobile;
    private String email;
    private Integer enable;
    private Integer sex;
    private Date birthday;
    private String password;
    private String avatar;
}
