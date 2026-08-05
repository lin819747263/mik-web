package com.mik.order.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    /**
     * 工单号
     */
    private String id;

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
     * 图片URL列表
     */
    private List<String> photos;

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
     * 承办部门ID
     */
    private Long deptId;

    /**
     * 承办单位
     */
    private String dept;

    /**
     * 工单状态
     */
    private String status;

    /**
     * 评价分
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 进度节点数组
     */
    private List<TimelineVO> timeline;

    /**
     * 当前任务名称
     */
    private String currentTaskName;

    /**
     * 当前任务处理人
     */
    private String currentTaskAssignee;

    /**
     * 指派处理人ID
     */
    private Long assigneeId;

    /**
     * 处理人姓名
     */
    private String assigneeName;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核人姓名
     */
    private String reviewerName;

    @Data
    public static class TimelineVO {
        /**
         * 时间
         */
        private String t;

        /**
         * 标题
         */
        private String title;

        /**
         * 描述
         */
        private String desc;

        /**
         * 状态
         */
        private String status;

        /**
         * 照片URL列表
         */
        private List<String> photos;
    }
}
