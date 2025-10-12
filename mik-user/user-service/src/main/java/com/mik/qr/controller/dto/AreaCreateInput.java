package com.mik.qr.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AreaCreateInput {
    private Long areaId;
    @NotBlank
    private String area;
    @NotBlank
    private String content;
}
