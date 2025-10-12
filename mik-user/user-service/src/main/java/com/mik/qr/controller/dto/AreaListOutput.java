package com.mik.qr.controller.dto;

import com.mybatisflex.annotation.Column;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class AreaListOutput {
    private Long areaId;
    private String uid;
    private String area;
    private String content;
    private String qrUrl;
    private Date createTime;
    private Date updateTime;
}
