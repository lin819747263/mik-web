package com.mik.dept.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("dept_permission")
public class DeptPermission extends BaseEntity {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long deptId;
    private Long pId;
}
