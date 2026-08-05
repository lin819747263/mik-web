package com.mik.dept.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeptTreeDTO {
    private Long deptId;
    private String deptName;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private Integer enable;
    private List<DeptTreeDTO> children = new ArrayList<>();
}
