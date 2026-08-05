package com.mik.dept.dto;

import lombok.Data;

@Data
public class DeptCreateCommand {
    private Long deptId;
    private String deptName;
    private Long parentId;
    private Integer sort;
    private Integer enable;
    private String remark;
    private Long receiverId;
}
