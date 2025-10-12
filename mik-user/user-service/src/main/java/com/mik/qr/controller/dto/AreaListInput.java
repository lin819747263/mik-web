package com.mik.qr.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AreaListInput {
    private String area;
    private String startTime;
    private String endTime;
}
