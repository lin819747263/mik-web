package com.mik.sys.controller.dto;

import lombok.Data;

import java.util.Date;

@Data
public class OperationLogListInput {
    private String keyword;
    private Date startTime;
    private Date endTime;
}
