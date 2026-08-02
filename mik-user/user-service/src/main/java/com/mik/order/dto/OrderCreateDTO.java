package com.mik.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {

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
     * 承办单位
     */
    private String dept;
}
