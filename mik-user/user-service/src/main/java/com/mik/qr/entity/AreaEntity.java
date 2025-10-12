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
@Table("area")
public class AreaEntity extends BaseDelEntity {
    @Id(keyType = KeyType.Auto)
    private Long areaId;
    private Long staffId;
    private String uid;
    private String area;
    private String content;
    private String qrUrl;
}
