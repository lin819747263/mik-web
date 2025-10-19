package com.mik.user.controller.cqe;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class PermissionDTO {
    private Long pId;
    private String code;
    private String name;
    private Integer type;
    private Long parent;
    private String icon;
    private String path;
    private String url;
    private Integer sort;
    private Date createTime;
    private List<PermissionDTO> children = new ArrayList<>();
}
