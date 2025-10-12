package com.mik.excel.controller;

import lombok.Data;
import org.jxls.builder.JxlsOutputFile;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class ExcelController {



    @PostMapping("t1")
    public void test01() throws FileNotFoundException {
        Map<String, Object> data = new HashMap<>();
        List<Income> gen = DateGenrate.gen();
        data.put("employees", gen);
        JxlsPoiTemplateFillerBuilder.newInstance()
                .withTemplate(Objects.requireNonNull(this.getClass().getClassLoader().getResource("template.xlsx")).getPath())
                .build()
                .fill(data, new JxlsOutputFile(new File("E:\\report.xlsx")));
    }

}
