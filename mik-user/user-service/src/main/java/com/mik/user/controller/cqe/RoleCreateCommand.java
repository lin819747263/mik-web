package com.mik.user.controller.cqe;

import lombok.Data;

@Data
public class RoleCreateCommand {
    private Long roleId;
    private String roleName;
    private String pIds;

}
