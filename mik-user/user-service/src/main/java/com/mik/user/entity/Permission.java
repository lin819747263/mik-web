package com.mik.user.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("permission")
public class Permission extends BaseEntity {
    @Id(keyType = KeyType.Auto)
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
