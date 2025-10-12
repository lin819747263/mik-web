package com.mik.user.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("user_role")
public class UserRole extends BaseEntity {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long userId;
    private Long roleId;
}
