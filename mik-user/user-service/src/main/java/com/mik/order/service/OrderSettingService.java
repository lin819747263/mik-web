package com.mik.order.service;

import com.mik.order.entity.OrderDeptReceiver;
import com.mik.order.entity.OrderSetting;
import com.mik.order.mapper.OrderDeptReceiverMapper;
import com.mik.order.mapper.OrderSettingMapper;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderSettingService extends ServiceImpl<OrderSettingMapper, OrderSetting> {

    @Autowired
    private OrderDeptReceiverMapper orderDeptReceiverMapper;

    /**
     * 获取工单设置（只有一条记录）
     */
    public OrderSetting getSetting() {
        OrderSetting setting = getMapper().selectOneByQuery(
                QueryWrapper.create().limit(1));
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

    /**
     * 获取部门接收人配置列表
     */
    public List<OrderDeptReceiver> getDeptReceivers() {
        return orderDeptReceiverMapper.selectListByQuery(QueryWrapper.create());
    }

    /**
     * 根据部门ID获取接收人
     */
    public OrderDeptReceiver getDeptReceiverByDeptId(Long deptId) {
        return orderDeptReceiverMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(new QueryColumn("dept_id").eq(deptId))
                        .limit(1));
    }

    /**
     * 保存部门接收人配置（全量更新）
     */
    @Transactional
    public void saveDeptReceivers(List<OrderDeptReceiver> list) {
        // 先删除所有旧配置（添加永真条件满足 MyBatis-Flex 要求）
        orderDeptReceiverMapper.deleteByQuery(
                QueryWrapper.create().where(new QueryColumn("id").isNotNull()));
        // 保存新配置
        if (list != null && !list.isEmpty()) {
            for (OrderDeptReceiver receiver : list) {
                orderDeptReceiverMapper.insert(receiver);
            }
        }
    }

    /**
     * 删除部门接收人配置
     */
    public void deleteDeptReceiver(Long id) {
        orderDeptReceiverMapper.deleteById(id);
    }
}
