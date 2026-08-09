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
import java.util.Map;

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
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Boolean urgent) {
        List<OrderVO> orders = orderService.getAllOrders(status, dept, assigneeId, orderNo, urgent);
        return Result.success(orders);
    }

    /**
     * 指派工单
     */
    @PostMapping("/{id}/assign")
    public Result<OrderVO> assignOrder(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Long deptId = body.get("deptId") != null ? Long.valueOf(body.get("deptId").toString()) : null;
        String deptName = (String) body.get("deptName");
        Long assigneeId = body.get("assigneeId") != null ? Long.valueOf(body.get("assigneeId").toString()) : null;
        String assigneeName = (String) body.get("assigneeName");
        String desc = (String) body.get("desc");

        OrderVO orderVO = orderService.assignOrder(id, deptId, deptName, assigneeId, assigneeName, desc);
        return Result.success(orderVO);
    }

    /**
     * 提交审核
     */
    @PostMapping("/{id}/submit-review")
    public Result<OrderVO> submitReview(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String desc = (String) body.get("desc");
        List<String> photos = body.get("photos") != null ? (List<String>) body.get("photos") : null;

        OrderVO orderVO = orderService.submitReview(id, desc, photos);
        return Result.success(orderVO);
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    public Result<OrderVO> approveOrder(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String desc = (String) body.get("desc");

        OrderVO orderVO = orderService.approveOrder(id, desc);
        return Result.success(orderVO);
    }

    /**
     * 审核退回
     */
    @PostMapping("/{id}/reject")
    public Result<OrderVO> rejectOrder(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String desc = (String) body.get("desc");
        String targetStatus = (String) body.get("targetStatus");

        OrderVO orderVO = orderService.rejectOrder(id, desc, targetStatus);
        return Result.success(orderVO);
    }
}
