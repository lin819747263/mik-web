package com.mik.order.service;

import com.mik.order.entity.OrderSetting;
import com.mik.order.mapper.OrderSettingMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderSettingService extends ServiceImpl<OrderSettingMapper, OrderSetting> {

    /**
     * 获取工单设置（只有一条记录）
     */
    public OrderSetting getSetting() {
        OrderSetting setting = getMapper().selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create().limit(1));
        if (setting == null) {
            setting = new OrderSetting();
        }
        return setting;
    }

    /**
     * 保存工单设置
     */
    public void saveSetting(OrderSetting setting) {
        OrderSetting existing = getSetting();
        if (existing.getId() != null) {
            setting.setId(existing.getId());
            getMapper().update(setting);
        } else {
            getMapper().insert(setting);
        }
    }
}
