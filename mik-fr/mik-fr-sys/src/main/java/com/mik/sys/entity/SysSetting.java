package com.mik.sys.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("sys_setting")
public class SysSetting extends BaseEntity {
    @Id(keyType = KeyType.Auto)
    private Long settingId;
    private String sysName;
    private String logoUrl;
    private String loginUrl;
}
