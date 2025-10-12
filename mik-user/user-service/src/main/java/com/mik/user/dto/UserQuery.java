package com.mik.user.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserQuery {
    private String username;
    private Date startTime;
    private Date endTime;
}
