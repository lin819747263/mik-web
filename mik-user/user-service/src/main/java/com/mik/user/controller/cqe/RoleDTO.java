package com.mik.user.controller.cqe;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RoleDTO {
    private Long roleId;
    private String roleName;
    private List<Long> permissions;
    private Date createTime;
}
