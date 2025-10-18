package com.mik.qr.entity;

import com.mik.db.entity.BaseDelEntity;
import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("his_area")
public class HisAreaEntity extends BaseEntity {
    @Id(keyType = KeyType.Auto)
    private Long hisAreaId;
    private Long areaId;
    private String uid;
    private Long userId;
    private String area;
    private String content;
}
