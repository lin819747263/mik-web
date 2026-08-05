package com.mik.dept.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("department")
public class Department extends BaseEntity {
    @Id(keyType = KeyType.Auto)
    private Long deptId;
    private String deptName;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private Integer enable;
    private String remark;
    private Long receiverId;
}
