package com.mik.sys.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("sys_setting")
public class SysSetting extends BaseEntity {
    private Long settingId;
    private String sysName;
    private String logoUrl;
    private String loginUrl;
}
