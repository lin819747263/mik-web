package com.mik.order.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("order")
public class Order {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 工单号，格式 SY + yyyyMMdd + 4位随机
     */
    private String orderNo;

    /**
     * 一级分类id
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 二级细分
     */
    private String subName;

    /**
     * 文字描述
     */
    private String desc;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 网格名称
     */
    private String gridName;

    /**
     * 网格编码
     */
    private String gridCode;

    /**
     * 图片URL列表，JSON格式存储
     */
    private String photos;

    /**
     * 是否紧急
     */
    private Boolean urgent;

    /**
     * 是否匿名
     */
    private Boolean anonymous;

    /**
     * 联系手机号
     */
    private String phone;

    /**
     * 承办单位
     */
    private String dept;

    /**
     * 工单状态：pending/accepted/dispatched/processing/done/rated/rejected
     */
    private String status;

    /**
     * 评价分（评价后写入）
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 创建时间
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long userId;
}
