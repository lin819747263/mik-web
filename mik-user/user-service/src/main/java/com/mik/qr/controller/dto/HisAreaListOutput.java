package com.mik.qr.controller.dto;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class HisAreaListOutput {
    private Long hisAreaId;
    private Long areaId;
    private Long uid;
    private Long userId;
    private String userName;
    private String area;
    private String content;
    private Date createTime;
    private Date updateTime;
}
