package com.mik.order.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("order_timeline")
public class OrderTimeline {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 工单ID
     */
    private Long orderId;

    /**
     * 时间节点
     */
    private LocalDateTime time;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String desc;

    /**
     * 对应状态
     */
    private String status;

    /**
     * 照片URL列表，JSON格式存储
     */
    private String photos;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
