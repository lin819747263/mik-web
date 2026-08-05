package com.mik.order.controller;

import com.mik.core.pojo.Result;
import com.mik.order.entity.OrderSetting;
import com.mik.order.service.OrderSettingService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order/setting")
public class OrderSettingController {

    @Resource
    private OrderSettingService orderSettingService;

    /**
     * 获取工单设置
     */
    @GetMapping
    public Result<OrderSetting> getSetting() {
        return Result.success(orderSettingService.getSetting());
    }

    /**
     * 保存工单设置
     */
    @PostMapping
    public Result saveSetting(@RequestBody OrderSetting setting) {
        orderSettingService.saveSetting(setting);
        return Result.success();
    }
}
