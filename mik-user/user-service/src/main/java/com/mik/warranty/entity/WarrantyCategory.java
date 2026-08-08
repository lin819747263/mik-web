package com.mik.warranty.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("warranty_category")
public class WarrantyCategory extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 问题名称
     */
    private String name;

    /**
     * 父级ID（0为一级）
     */
    private Long parentId;

    /**
     * 层级（1/2）
     */
    private Integer level;

    /**
     * 负责部门ID
     */
    private Long deptId;

    /**
     * 负责部门名称
     */
    private String deptName;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 状态（0禁用/1启用）
     */
    private Integer enable;

    /**
     * 首页展示（0否/1是）
     */
    private Integer showOnHome;
}
