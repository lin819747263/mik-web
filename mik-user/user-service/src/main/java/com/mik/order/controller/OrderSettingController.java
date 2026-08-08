package com.mik.order.controller;

import com.mik.core.pojo.Result;
import com.mik.order.entity.OrderDeptReceiver;
import com.mik.order.entity.OrderSetting;
import com.mik.order.service.OrderSettingService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 获取部门接收人配置列表
     */
    @GetMapping("/dept-receiver")
    public Result<List<OrderDeptReceiver>> getDeptReceivers() {
        return Result.success(orderSettingService.getDeptReceivers());
    }

    /**
     * 保存部门接收人配置（全量更新）
     */
    @PostMapping("/dept-receiver")
    public Result saveDeptReceivers(@RequestBody List<OrderDeptReceiver> list) {
        orderSettingService.saveDeptReceivers(list);
        return Result.success();
    }

    /**
     * 删除部门接收人配置
     */
    @DeleteMapping("/dept-receiver/{id}")
    public Result deleteDeptReceiver(@PathVariable Long id) {
        orderSettingService.deleteDeptReceiver(id);
        return Result.success();
    }
}
