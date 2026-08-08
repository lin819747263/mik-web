package com.mik.warranty.dto;

import lombok.Data;

import java.util.List;

@Data
public class WarrantyCategoryDTO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private Long deptId;
    private String deptName;
    private Integer sort;
    private Integer enable;
    private Integer showOnHome;

    /**
     * 子分类
     */
    private List<WarrantyCategoryDTO> children;
}
