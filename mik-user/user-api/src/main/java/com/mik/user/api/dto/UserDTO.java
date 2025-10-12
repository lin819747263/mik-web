package com.mik.user.api.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private Integer age;
    private String email;
    private Integer port;
}
