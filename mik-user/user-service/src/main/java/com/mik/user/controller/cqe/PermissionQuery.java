package com.mik.user.controller.cqe;

import lombok.Data;

@Data
public class PermissionQuery {
    private String code;
    private String name;
    private Integer type;
}
