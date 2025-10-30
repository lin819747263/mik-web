package com.mik.qr.controller.dto;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QrCodeImportDTO {

    @ExcelProperty("区域")
    private String area;

    @ExcelProperty("二维码内容")
    private String content;
}
