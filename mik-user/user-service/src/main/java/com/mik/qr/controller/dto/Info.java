package com.mik.qr.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Info {
    private String name;
    private String telephone;
}
