package com.mik.order.controller;

import com.mik.core.pojo.Result;
import com.mik.order.dto.OrderCreateDTO;
import com.mik.order.dto.OrderRateDTO;
import com.mik.order.dto.OrderStatusUpdateDTO;
import com.mik.order.dto.OrderVO;
import com.mik.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建工单（市民上报）
     */
    @PostMapping
    public Result<OrderVO> createOrder(@RequestBody OrderCreateDTO dto) {
        OrderVO orderVO = orderService.createOrder(dto);
        return Result.success(orderVO);
    }

    /**
     * 查询工单列表
     */
    @GetMapping
    public Result<List<OrderVO>> getOrders(@RequestParam(required = false, defaultValue = "all") String status) {
        List<OrderVO> orders = orderService.getOrders(status);
        return Result.success(orders);
    }

    /**
     * 查询工单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable String id) {
        OrderVO orderVO = orderService.getOrderDetailByOrderNo(id);
        return Result.success(orderVO);
    }

    /**
     * 评价工单
     */
    @PostMapping("/{id}/rate")
    public Result<OrderVO> rateOrder(@PathVariable String id, @RequestBody OrderRateDTO dto) {
        OrderVO orderVO = orderService.rateOrderByOrderNo(id, dto);
        return Result.success(orderVO);
    }

    /**
     * 更新工单状态（运维人员操作）
     * 受理、派单、处理、办结、退回
     */
    @PostMapping("/{id}/status")
    public Result<OrderVO> updateOrderStatus(@PathVariable String id, @RequestBody OrderStatusUpdateDTO dto) {
        OrderVO orderVO = orderService.updateOrderStatus(id, dto);
        return Result.success(orderVO);
    }

    /**
     * 查询所有工单（运维人员/管理员）
     */
    @GetMapping("/all")
    public Result<List<OrderVO>> getAllOrders(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) String dept) {
        List<OrderVO> orders = orderService.getAllOrders(status, dept);
        return Result.success(orders);
    }
}
