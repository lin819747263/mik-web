package com.mik.qr.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AreaListOutput {
    private Long areaId;
    private String uid;
    private String area;
    private String content;
    private String qrUrl;
}
