package com.mik.qr.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StaffCreateInput {
    private Long staffId;
    private String avatar;
    @NotBlank
    private String contact;
    @NotBlank
    private String telephone;
}
