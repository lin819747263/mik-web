package com.mik.user.controller.cqe;

import lombok.Data;

@Data
public class PermissionCreateCommand {
    private Long pId;
    private String code;
    private String name;
    private Integer type;
    private Long parent;
    private String icon;
    private String path;
    private String url;
    private Integer sort;
}
