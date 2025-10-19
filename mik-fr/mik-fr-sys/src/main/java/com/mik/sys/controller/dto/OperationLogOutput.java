package com.mik.sys.controller.dto;

import lombok.Data;

import java.util.Date;

@Data
public class OperationLogOutput {
    private Long operationId;
    private Long userId;
    private String username;
    private String operationName;
    private String ip;
    private String url;
    private String param;
    private Integer result;
    private Date createTime;
}
