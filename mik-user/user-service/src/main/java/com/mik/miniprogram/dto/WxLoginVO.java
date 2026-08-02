package com.mik.miniprogram.dto;

import lombok.Data;

import java.util.List;

@Data
public class WxLoginVO {

    /**
     * JWT token
     */
    private String token;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 微信unionid（如果有）
     */
    private String unionid;

    /**
     * 用户角色：citizen-市民, operator-运维人员, admin-管理员
     */
    private String role;

    /**
     * 用户角色名称列表
     */
    private List<String> roleNames;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;
}
