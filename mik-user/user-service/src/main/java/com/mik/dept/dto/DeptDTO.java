package com.mik.dept.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DeptDTO {
    private Long deptId;
    private String deptName;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private Integer enable;
    private String remark;
    private Date createTime;
    private List<Long> permissionIds;
}
