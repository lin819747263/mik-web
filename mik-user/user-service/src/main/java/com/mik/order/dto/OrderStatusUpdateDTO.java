package com.mik.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderStatusUpdateDTO {

    /**
     * 目标状态：accepted-受理, dispatched-派单, processing-处理中, done-办结, rejected-退回
     */
    private String status;

    /**
     * 操作说明
     */
    private String desc;

    /**
     * 派单时指定的承办部门
     */
    private String dept;

    /**
     * 办结时上传的照片URL列表
     */
    private List<String> photos;
}
