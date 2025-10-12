package com.mik.qr.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("staff")
public class StaffEntity extends BaseEntity {
    @Id(keyType = KeyType.Auto)
    private Long staffId;
    private String avatar;
    private String contact;
    private String telephone;
}
