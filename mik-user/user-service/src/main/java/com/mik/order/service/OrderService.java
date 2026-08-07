package com.mik.order.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.mik.core.exception.ServiceException;
import com.mik.order.dto.OrderCreateDTO;
import com.mik.order.dto.OrderRateDTO;
import com.mik.order.dto.OrderStatusUpdateDTO;
import com.mik.order.dto.OrderVO;
import com.mik.order.entity.Order;
import com.mik.order.entity.OrderSetting;
import com.mik.order.entity.OrderTimeline;
import com.mik.order.mapper.OrderMapper;
import com.mik.order.mapper.OrderTimelineMapper;
import com.mik.security.UserContext;
import com.mik.user.controller.cqe.RoleDTO;
import com.mik.user.service.RoleService;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderTimelineMapper orderTimelineMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OrderFlowService orderFlowService;

    @Autowired
    private OrderSettingService orderSettingService;

    @Autowired
    private com.mik.user.mapper.UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 创建工单（兼容小程序接口）
     */
    @Transactional
    public OrderVO createOrder(OrderCreateDTO dto) {
        // 生成工单号: SY + yyyyMMdd + 4位随机
        String orderNo = generateOrderNo();
        Long userId = UserContext.getUserId();

        // 创建工单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setCategoryId(dto.getCategoryId());
        order.setCategoryName(dto.getCategoryName());
        order.setSubName(dto.getSubName());
        order.setDesc(dto.getDesc());
        order.setAddress(dto.getAddress());
        order.setLatitude(dto.getLatitude());
        order.setLongitude(dto.getLongitude());
        order.setGridName(dto.getGridName());
        order.setGridCode(dto.getGridCode());
        order.setPhotos(dto.getPhotos() != null ? JSON.toJSONString(dto.getPhotos()) : "[]");
        order.setUrgent(dto.getUrgent() != null ? dto.getUrgent() : false);
        order.setAnonymous(dto.getAnonymous() != null ? dto.getAnonymous() : false);
        order.setPhone(dto.getPhone());
        order.setDept(dto.getDept());
        order.setStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setUserId(userId);

        // 从工单设置中获取默认指派人员和审核人员
        OrderSetting setting = orderSettingService.getSetting();
        if (setting.getDefaultAssigneeId() != null) {
            order.setAssigneeId(setting.getDefaultAssigneeId());
            order.setAssigneeName(setting.getDefaultAssigneeName());
            order.setStatus("assigned");
        }
        if (setting.getDefaultReviewerId() != null) {
            order.setReviewerId(setting.getDefaultReviewerId());
            order.setReviewerName(setting.getDefaultReviewerName());
        }

        orderMapper.insert(order);

        // 启动流程
        String processInstanceId = orderFlowService.startOrderProcess(
                order.getId(), orderNo, userId);
        order.setProcessInstanceId(processInstanceId);
        orderMapper.update(order);

        // 创建初始时间线
        addTimeline(order.getId(), "市民提交", "pending",
                "诉求已进入12345受理池，等待话务中心分派", null);

        // 返回VO
        return convertToVO(order);
    }

    /**
     * 查询工单列表（市民查看自己的工单）
     */
    public List<OrderVO> getOrders(String status) {
        Long userId = UserContext.getUserId();
        QueryCondition condition = QueryCondition.create(new QueryColumn("user_id"), "=", userId);

        // 根据状态过滤
        if (StrUtil.isNotBlank(status) && !"all".equals(status)) {
            if ("doing".equals(status)) {
                condition.and(QueryCondition.create(new QueryColumn("status"), "in",
                        Arrays.asList("pending", "accepted", "assigned", "processing", "reviewing")));
            } else if ("finished".equals(status)) {
                condition.and(QueryCondition.create(new QueryColumn("status"), "in",
                        Arrays.asList("done", "rated")));
            } else {
                condition.and(QueryCondition.create(new QueryColumn("status"), "=", status));
            }
        }

        QueryWrapper wrapper = QueryWrapper.create().select().from("order")
                .where(condition)
                .orderBy(new QueryColumn("created_at").desc());

        List<Order> orders = orderMapper.selectListByQuery(wrapper);
        return orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询工单详情
     */
    public OrderVO getOrderDetail(Long orderId) {
        Long userId = UserContext.getUserId();

        Order order = orderMapper.selectOneById(orderId);
        if (order == null) {
            throw new ServiceException("工单不存在");
        }

        // 验证是否是当前用户的工单
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权查看此工单");
        }

        return convertToVO(order);
    }

    /**
     * 根据工单号查询详情（兼容小程序接口）
     */
    public OrderVO getOrderDetailByOrderNo(String orderNo) {
        Long userId = UserContext.getUserId();

        Order order = getOrderByNo(orderNo);

        // 验证权限：市民只能查看自己的工单，运维人员可以查看所有工单
        if (!order.getUserId().equals(userId) && !isOperator(userId)) {
            throw new ServiceException("无权查看此工单");
        }

        return convertToVO(order);
    }

    /**
     * 受理工单
     */
    @Transactional
    public OrderVO acceptOrder(String orderNo, String desc) {
        Long userId = UserContext.getUserId();

        // 验证用户是否有运维权限
        if (!isOperator(userId)) {
            throw new ServiceException("无权执行此操作");
        }

        Order order = getOrderByNo(orderNo);

        // 验证状态
        if (!"pending".equals(order.getStatus())) {
            throw new ServiceException("当前状态不允许受理");
        }

        // 推进流程
        orderFlowService.acceptOrder(order.getProcessInstanceId(), userId, true, desc);

        // 更新工单状态
        order.setStatus("accepted");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);

        // 记录时间线
        addTimeline(order.getId(), "已受理", "accepted",
                desc != null ? desc : "工单已受理，等待派单", null);

        return convertToVO(order);
    }

    /**
     * 指派工单
     */
    @Transactional
    public OrderVO assignOrder(String orderNo, Long deptId, String deptName,
                              Long assigneeId, String assigneeName, String desc) {
        Long userId = UserContext.getUserId();

        // 验证用户是否有运维权限
        if (!isOperator(userId)) {
            throw new ServiceException("无权执行此操作");
        }

        Order order = getOrderByNo(orderNo);

        // 验证状态：已办结的工单不允许指派
        if ("done".equals(order.getStatus()) || "rated".equals(order.getStatus())) {
            throw new ServiceException("已办结的工单不允许指派");
        }

        // 推进流程（如果有流程实例）
        if (order.getProcessInstanceId() != null) {
            try {
                orderFlowService.assignOrder(order.getProcessInstanceId(),
                        userId, assigneeId, deptId, desc);
            } catch (Exception e) {
                // 流程推进失败不影响指派操作
            }
        }

        // 更新工单
        order.setDeptId(deptId);
        if (deptName != null) {
            order.setDept(deptName);
        }
        order.setAssigneeId(assigneeId);
        order.setAssigneeName(assigneeName);
        order.setStatus("assigned");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);

        // 记录时间线
        String assigneeInfo = assigneeName != null ? "，处理人：" + assigneeName : "";
        addTimeline(order.getId(), "已指派", "assigned",
                desc != null ? desc : "工单已指派至" + (deptName != null ? deptName : "承办部门") + assigneeInfo,
                null);

        return convertToVO(order);
    }

    /**
     * 开始处理
     */
    @Transactional
    public OrderVO startProcess(String orderNo, String desc) {
        Long userId = UserContext.getUserId();
        Order order = getOrderByNo(orderNo);

        // 验证状态
        if (!"assigned".equals(order.getStatus())) {
            throw new ServiceException("当前状态不允许开始处理");
        }

        // 推进流程
        orderFlowService.startProcess(order.getProcessInstanceId(), userId);

        // 更新工单状态
        order.setStatus("processing");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);

        // 记录时间线
        addTimeline(order.getId(), "处理中", "processing",
                desc != null ? desc : "处理人员正在处理", null);

        return convertToVO(order);
    }

    /**
     * 结办（提交审核）
     */
    @Transactional
    public OrderVO submitReview(String orderNo, String desc, List<String> photos) {
        Long userId = UserContext.getUserId();
        Order order = getOrderByNo(orderNo);

        // 验证状态
        if (!"processing".equals(order.getStatus())) {
            throw new ServiceException("当前状态不允许结办");
        }

        // 推进流程（容错处理）
        if (order.getProcessInstanceId() != null) {
            try {
                orderFlowService.submitReview(order.getProcessInstanceId(), userId, desc);
            } catch (Exception e) {
                // 流程推进失败不影响状态更新
            }
        }

        // 获取工单设置中的审核人员
        OrderSetting setting = orderSettingService.getSetting();
        if (setting.getDefaultReviewerId() != null) {
            order.setReviewerId(setting.getDefaultReviewerId());
            order.setReviewerName(setting.getDefaultReviewerName());
        }

        // 更新工单状态
        order.setStatus("reviewing");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);

        // 记录时间线
        String reviewerInfo = order.getReviewerName() != null ? "，审核人：" + order.getReviewerName() : "";
        addTimeline(order.getId(), "结办", "reviewing",
                (desc != null ? desc : "工单已结办，等待审核") + reviewerInfo, photos);

        return convertToVO(order);
    }

    /**
     * 审核通过
     */
    @Transactional
    public OrderVO approveOrder(String orderNo, String desc) {
        Long userId = UserContext.getUserId();

        // 验证用户是否有审核权限
        if (!isOperator(userId)) {
            throw new ServiceException("无权执行此操作");
        }

        Order order = getOrderByNo(orderNo);

        // 验证状态
        if (!"reviewing".equals(order.getStatus())) {
            throw new ServiceException("当前状态不允许审核");
        }

        // 推进流程（容错处理）
        if (order.getProcessInstanceId() != null) {
            try {
                orderFlowService.reviewOrder(order.getProcessInstanceId(),
                        userId, true, null, desc);
            } catch (Exception e) {
                // 流程推进失败不影响状态更新
            }
        }

        // 获取审核人真实姓名
        com.mik.user.entity.User reviewer = userMapper.selectOneById(userId);
        String reviewerName = reviewer != null ? (reviewer.getNickname() != null ? reviewer.getNickname() : reviewer.getUsername()) : "审核员";

        // 更新工单状态
        order.setStatus("done");
        order.setReviewerId(userId);
        order.setReviewerName(reviewerName);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);

        // 记录时间线
        addTimeline(order.getId(), "审核通过", "done",
                desc != null ? desc : "审核通过，工单已办结", null);

        return convertToVO(order);
    }

    /**
     * 审核退回
     */
    @Transactional
    public OrderVO rejectOrder(String orderNo, String desc, String targetStatus) {
        Long userId = UserContext.getUserId();

        // 验证用户是否有审核权限
        if (!isOperator(userId)) {
            throw new ServiceException("无权执行此操作");
        }

        Order order = getOrderByNo(orderNo);

        // 验证状态
        if (!"reviewing".equals(order.getStatus())) {
            throw new ServiceException("当前状态不允许退回");
        }

        // 确定退回目标
        String rejectTarget = "assign"; // 默认退回至指派
        String newStatus = "assigned";
        if ("process".equals(targetStatus)) {
            rejectTarget = "process";
            newStatus = "assigned"; // 退回至处理时，状态回到 assigned
        }

        // 推进流程
        orderFlowService.reviewOrder(order.getProcessInstanceId(),
                userId, false, rejectTarget, desc);

        // 更新工单状态
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);

        // 记录时间线
        addTimeline(order.getId(), "审核退回", newStatus,
                desc != null ? desc : "审核未通过，退回重新处理", null);

        return convertToVO(order);
    }

    /**
     * 评价工单（兼容小程序接口）
     */
    @Transactional
    public OrderVO rateOrder(Long orderId, OrderRateDTO dto) {
        Long userId = UserContext.getUserId();

        Order order = orderMapper.selectOneById(orderId);
        if (order == null) {
            throw new ServiceException("工单不存在");
        }

        // 验证是否是当前用户的工单
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权评价此工单");
        }

        // 验证状态是否可评价
        if (!"done".equals(order.getStatus())) {
            throw new ServiceException("当前状态不可评价");
        }

        // 验证评分范围
        if (dto.getScore() < 1 || dto.getScore() > 5) {
            throw new ServiceException("评分范围为1-5");
        }

        // 更新工单
        order.setScore(dto.getScore());
        order.setComment(dto.getComment());
        order.setStatus("rated");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);

        // 添加评价时间线
        addTimeline(order.getId(), "市民评价", "rated",
                dto.getScore() + "星 " + (dto.getComment() != null ? dto.getComment() : ""), null);

        return convertToVO(order);
    }

    /**
     * 根据工单号评价工单（兼容小程序接口）
     */
    @Transactional
    public OrderVO rateOrderByOrderNo(String orderNo, OrderRateDTO dto) {
        Order order = getOrderByNo(orderNo);
        return rateOrder(order.getId(), dto);
    }

    /**
     * 更新工单状态（兼容旧接口）
     */
    @Transactional
    public OrderVO updateOrderStatus(String orderNo, OrderStatusUpdateDTO dto) {
        String targetStatus = dto.getStatus();

        // 根据目标状态调用对应方法
        switch (targetStatus) {
            case "accepted":
                return acceptOrder(orderNo, dto.getDesc());
            case "assigned":
                return assignOrder(orderNo, null, dto.getDept(), null, null, dto.getDesc());
            case "processing":
                return startProcess(orderNo, dto.getDesc());
            case "reviewing":
                return submitReview(orderNo, dto.getDesc(), dto.getPhotos());
            case "done":
                return approveOrder(orderNo, dto.getDesc());
            case "rejected":
                return rejectOrder(orderNo, dto.getDesc(), null);
            default:
                throw new ServiceException("不支持的状态变更");
        }
    }

    /**
     * 查询所有工单（运维人员/管理员）
     */
    public List<OrderVO> getAllOrders(String status, String dept) {
        Long userId = UserContext.getUserId();

        // 验证用户是否有运维权限
        if (!isOperator(userId)) {
            throw new ServiceException("无权执行此操作");
        }

        // 构建查询条件
        QueryWrapper wrapper = QueryWrapper.create().select().from("order");

        // 根据状态过滤
        if (StrUtil.isNotBlank(status) && !"all".equals(status)) {
            if ("doing".equals(status)) {
                wrapper.and(new QueryColumn("status").in(
                        Arrays.asList("pending", "accepted", "assigned", "processing", "reviewing")));
            } else if ("finished".equals(status)) {
                wrapper.and(new QueryColumn("status").in(
                        Arrays.asList("done", "rated")));
            } else {
                wrapper.and(new QueryColumn("status").eq(status));
            }
        }

        // 根据部门过滤
        if (StrUtil.isNotBlank(dept)) {
            wrapper.and(new QueryColumn("dept").eq(dept));
        }

        wrapper.orderBy(new QueryColumn("created_at").desc());

        List<Order> orders = orderMapper.selectListByQuery(wrapper);
        return orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // ==================== 辅助方法 ====================

    /**
     * 根据工单号获取工单
     */
    private Order getOrderByNo(String orderNo) {
        Order order = orderMapper.selectOneByCondition(
                QueryCondition.create(new QueryColumn("order_no"), "=", orderNo));
        if (order == null) {
            throw new ServiceException("工单不存在");
        }
        return order;
    }

    /**
     * 判断用户是否是运维人员
     */
    private boolean isOperator(Long userId) {
        List<RoleDTO> roles = roleService.listUserRoles(userId);
        return roles.stream().anyMatch(r ->
                "operator".equals(r.getRoleName()) || "admin".equals(r.getRoleName()));
    }

    /**
     * 添加时间线记录
     */
    private void addTimeline(Long orderId, String title, String status, String desc, List<String> photos) {
        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrderId(orderId);
        timeline.setTime(LocalDateTime.now());
        timeline.setTitle(title);
        timeline.setDesc(desc);
        timeline.setStatus(status);
        timeline.setCreatedAt(LocalDateTime.now());
        if (photos != null && !photos.isEmpty()) {
            timeline.setPhotos(JSON.toJSONString(photos));
        }
        orderTimelineMapper.insert(timeline);
    }

    /**
     * 生成工单号
     */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "SY" + dateStr + random;
    }

    /**
     * 转换为VO
     */
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getOrderNo());
        vo.setCategoryId(order.getCategoryId());
        vo.setCategoryName(order.getCategoryName());
        vo.setSubName(order.getSubName());
        vo.setDesc(order.getDesc());
        vo.setAddress(order.getAddress());
        vo.setLatitude(order.getLatitude());
        vo.setLongitude(order.getLongitude());
        vo.setGridName(order.getGridName());
        vo.setGridCode(order.getGridCode());
        vo.setPhotos(JSON.parseArray(order.getPhotos(), String.class));
        vo.setUrgent(order.getUrgent());
        vo.setAnonymous(order.getAnonymous());
        vo.setPhone(order.getPhone());
        vo.setDept(order.getDept());
        vo.setDeptId(order.getDeptId());
        vo.setAssigneeId(order.getAssigneeId());
        vo.setAssigneeName(order.getAssigneeName());
        vo.setReviewerId(order.getReviewerId());
        vo.setReviewerName(order.getReviewerName());
        vo.setStatus(order.getStatus());
        vo.setScore(order.getScore());
        vo.setComment(order.getComment());
        vo.setCreatedAt(order.getCreatedAt().format(FORMATTER));

        // 查询时间线
        List<OrderTimeline> timelines = orderTimelineMapper.selectListByCondition(
                QueryCondition.create(new QueryColumn("order_id"), "=", order.getId()));

        List<OrderVO.TimelineVO> timelineVOs = timelines.stream()
                .sorted(Comparator.comparing(OrderTimeline::getTime))
                .map(t -> {
                    OrderVO.TimelineVO tvo = new OrderVO.TimelineVO();
                    tvo.setT(t.getTime().format(FORMATTER));
                    tvo.setTitle(t.getTitle());
                    tvo.setDesc(t.getDesc());
                    tvo.setStatus(t.getStatus());
                    if (t.getPhotos() != null && !t.getPhotos().isEmpty()) {
                        tvo.setPhotos(JSON.parseArray(t.getPhotos(), String.class));
                    }
                    return tvo;
                })
                .collect(Collectors.toList());
        vo.setTimeline(timelineVOs);

        // 获取当前流程任务信息
        if (order.getProcessInstanceId() != null) {
            Task currentTask = orderFlowService.getCurrentTask(order.getProcessInstanceId());
            if (currentTask != null) {
                vo.setCurrentTaskName(currentTask.getName());
                vo.setCurrentTaskAssignee(currentTask.getAssignee());
            }
        }

        return vo;
    }
}
