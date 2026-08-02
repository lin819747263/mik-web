package com.mik.order.dto;

import lombok.Data;

@Data
public class OrderRateDTO {

    /**
     * 评价分 1-5
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String comment;
}
