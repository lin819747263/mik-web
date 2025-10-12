package com.mik.sys.controller.dto;

import lombok.Data;

@Data
public class SysSettingInput {
    private Long settingId;
    private String sysName;
    private String logoUrl;
    private String loginUrl;
}
