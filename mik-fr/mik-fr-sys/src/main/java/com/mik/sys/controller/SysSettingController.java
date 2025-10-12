package com.mik.sys.controller;

import com.mik.core.pojo.Result;
import com.mik.sys.controller.dto.SysSettingInput;
import com.mik.sys.entity.SysSetting;
import com.mik.sys.service.SysSettingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sys/setting")
public class SysSettingController {

    @Autowired
    SysSettingService sysSettingService;

    @PostMapping("saveSetting")
    public Result<Void> saveSetting(SysSettingInput input) {
        SysSetting sysSetting = new SysSetting();
        BeanUtils.copyProperties(input, sysSetting);
        sysSettingService.save(sysSetting);
        return Result.success();
    }
}
