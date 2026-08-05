package com.mik.order.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("order_setting")
public class OrderSetting extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 默认指派人员ID
     */
    private Long defaultAssigneeId;

    /**
     * 默认指派人员姓名
     */
    private String defaultAssigneeName;

    /**
     * 默认审核人员ID
     */
    private Long defaultReviewerId;

    /**
     * 默认审核人员姓名
     */
    private String defaultReviewerName;
}
