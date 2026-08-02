package com.mik.order.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.mik.core.exception.ServiceException;
import com.mik.order.dto.OrderCreateDTO;
import com.mik.order.dto.OrderRateDTO;
import com.mik.order.dto.OrderStatusUpdateDTO;
import com.mik.order.dto.OrderVO;
import com.mik.order.entity.Order;
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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 创建工单
     */
    @Transactional
    public OrderVO createOrder(OrderCreateDTO dto) {
        // 生成工单号: SY + yyyyMMdd + 4位随机
        String orderNo = generateOrderNo();

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
        order.setUserId(UserContext.getUserId());

        orderMapper.insert(order);

        // 创建初始时间线
        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrderId(order.getId());
        timeline.setTime(LocalDateTime.now());
        timeline.setTitle("市民提交");
        timeline.setDesc("诉求已进入12345受理池，等待话务中心分派");
        timeline.setStatus("pending");
        timeline.setCreatedAt(LocalDateTime.now());
        orderTimelineMapper.insert(timeline);

        // 返回VO
        return convertToVO(order, Collections.singletonList(timeline));
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
                        Arrays.asList("pending", "accepted", "dispatched", "processing")));
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

        // 批量查询时间线
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderTimeline>> timelineMap;
        if (!orderIds.isEmpty()) {
            List<OrderTimeline> timelines = orderTimelineMapper.selectListByQuery(
                    QueryWrapper.create().from("order_timeline")
                            .where(new QueryColumn("order_id").in(orderIds)));
            timelineMap = timelines.stream()
                    .collect(Collectors.groupingBy(OrderTimeline::getOrderId));
        } else {
            timelineMap = new HashMap<>();
        }

        // 转换VO
        return orders.stream()
                .map(order -> convertToVO(order, timelineMap.getOrDefault(order.getId(), Collections.emptyList())))
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

        // 查询时间线
        List<OrderTimeline> timelines = orderTimelineMapper.selectListByCondition(
                QueryCondition.create(new QueryColumn("order_id"), "=", orderId));

        return convertToVO(order, timelines);
    }

    /**
     * 根据工单号查询详情
     */
    public OrderVO getOrderDetailByOrderNo(String orderNo) {
        Long userId = UserContext.getUserId();

        Order order = orderMapper.selectOneByCondition(
                QueryCondition.create(new QueryColumn("order_no"), "=", orderNo));
        if (order == null) {
            throw new ServiceException("工单不存在");
        }

        // 验证权限：市民只能查看自己的工单，运维人员可以查看所有工单
        if (!order.getUserId().equals(userId) && !isOperator(userId)) {
            throw new ServiceException("无权查看此工单");
        }

        // 查询时间线
        List<OrderTimeline> timelines = orderTimelineMapper.selectListByCondition(
                QueryCondition.create(new QueryColumn("order_id"), "=", order.getId()));

        return convertToVO(order, timelines);
    }

    /**
     * 评价工单
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
        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrderId(order.getId());
        timeline.setTime(LocalDateTime.now());
        timeline.setTitle("市民评价");
        timeline.setDesc(dto.getScore() + "星 " + (dto.getComment() != null ? dto.getComment() : ""));
        timeline.setStatus("rated");
        timeline.setCreatedAt(LocalDateTime.now());
        orderTimelineMapper.insert(timeline);

        // 查询时间线
        List<OrderTimeline> timelines = orderTimelineMapper.selectListByCondition(
                QueryCondition.create(new QueryColumn("order_id"), "=", orderId));

        return convertToVO(order, timelines);
    }

    /**
     * 根据工单号评价工单
     */
    @Transactional
    public OrderVO rateOrderByOrderNo(String orderNo, OrderRateDTO dto) {
        Long userId = UserContext.getUserId();

        Order order = orderMapper.selectOneByCondition(
                QueryCondition.create(new QueryColumn("order_no"), "=", orderNo));
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
        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrderId(order.getId());
        timeline.setTime(LocalDateTime.now());
        timeline.setTitle("市民评价");
        timeline.setDesc(dto.getScore() + "星 " + (dto.getComment() != null ? dto.getComment() : ""));
        timeline.setStatus("rated");
        timeline.setCreatedAt(LocalDateTime.now());
        orderTimelineMapper.insert(timeline);

        // 查询时间线
        List<OrderTimeline> timelines = orderTimelineMapper.selectListByCondition(
                QueryCondition.create(new QueryColumn("order_id"), "=", order.getId()));

        return convertToVO(order, timelines);
    }

    /**
     * 更新工单状态（运维人员操作）
     */
    @Transactional
    public OrderVO updateOrderStatus(String orderNo, OrderStatusUpdateDTO dto) {
        Long userId = UserContext.getUserId();

        // 验证用户是否有运维权限
        if (!isOperator(userId)) {
            throw new ServiceException("无权执行此操作");
        }

        Order order = orderMapper.selectOneByCondition(
                QueryCondition.create(new QueryColumn("order_no"), "=", orderNo));
        if (order == null) {
            throw new ServiceException("工单不存在");
        }

        String targetStatus = dto.getStatus();
        String currentStatus = order.getStatus();

        // 验证状态流转是否合法
        validateStatusTransition(currentStatus, targetStatus);

        // 根据操作类型设置不同的标题
        String title = getStatusTitle(targetStatus);
        String desc = dto.getDesc() != null ? dto.getDesc() : getDefaultDesc(targetStatus);

        // 如果是派单操作，更新承办部门
        if ("dispatched".equals(targetStatus) && dto.getDept() != null) {
            order.setDept(dto.getDept());
        }

        // 如果是办结操作且有照片，更新照片
        if ("done".equals(targetStatus) && dto.getPhotos() != null && !dto.getPhotos().isEmpty()) {
            // 获取现有照片
            List<String> existingPhotos = new ArrayList<>();
            if (order.getPhotos() != null && !order.getPhotos().isEmpty()) {
                existingPhotos = JSON.parseArray(order.getPhotos(), String.class);
            }
            // 添加新照片
            existingPhotos.addAll(dto.getPhotos());
            order.setPhotos(JSON.toJSONString(existingPhotos));
        }

        // 更新工单状态
        order.setStatus(targetStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);

        // 添加时间线
        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrderId(order.getId());
        timeline.setTime(LocalDateTime.now());
        timeline.setTitle(title);
        timeline.setDesc(desc);
        timeline.setStatus(targetStatus);
        timeline.setCreatedAt(LocalDateTime.now());
        // 如果是办结操作且有照片，保存到时间线
        if ("done".equals(targetStatus) && dto.getPhotos() != null && !dto.getPhotos().isEmpty()) {
            timeline.setPhotos(JSON.toJSONString(dto.getPhotos()));
        }
        orderTimelineMapper.insert(timeline);

        // 查询时间线
        List<OrderTimeline> timelines = orderTimelineMapper.selectListByCondition(
                QueryCondition.create(new QueryColumn("order_id"), "=", order.getId()));

        return convertToVO(order, timelines);
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
                        Arrays.asList("pending", "accepted", "dispatched", "processing")));
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

        // 批量查询时间线
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderTimeline>> timelineMap;
        if (!orderIds.isEmpty()) {
            List<OrderTimeline> timelines = orderTimelineMapper.selectListByQuery(
                    QueryWrapper.create().from("order_timeline")
                            .where(new QueryColumn("order_id").in(orderIds)));
            timelineMap = timelines.stream()
                    .collect(Collectors.groupingBy(OrderTimeline::getOrderId));
        } else {
            timelineMap = new HashMap<>();
        }

        // 转换VO
        return orders.stream()
                .map(order -> convertToVO(order, timelineMap.getOrDefault(order.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
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
     * 验证状态流转是否合法
     */
    private void validateStatusTransition(String current, String target) {
        // 定义合法的状态流转
        Map<String, List<String>> validTransitions = new HashMap<>();
        validTransitions.put("pending", Arrays.asList("accepted", "rejected"));
        validTransitions.put("accepted", Arrays.asList("dispatched", "rejected"));
        validTransitions.put("dispatched", Arrays.asList("processing"));
        validTransitions.put("processing", Arrays.asList("done"));

        List<String> allowed = validTransitions.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new ServiceException("当前状态不允许执行此操作");
        }
    }

    /**
     * 获取状态标题
     */
    private String getStatusTitle(String status) {
        switch (status) {
            case "accepted": return "已受理";
            case "dispatched": return "已派单";
            case "processing": return "处理中";
            case "done": return "已办结";
            case "rejected": return "已退回";
            default: return "状态变更";
        }
    }

    /**
     * 获取默认描述
     */
    private String getDefaultDesc(String status) {
        switch (status) {
            case "accepted": return "工单已受理，正在审核";
            case "dispatched": return "工单已派发至承办部门";
            case "processing": return "承办部门正在处理";
            case "done": return "工单已办结";
            case "rejected": return "工单已退回";
            default: return "";
        }
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
    private OrderVO convertToVO(Order order, List<OrderTimeline> timelines) {
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
        vo.setStatus(order.getStatus());
        vo.setScore(order.getScore());
        vo.setComment(order.getComment());
        vo.setCreatedAt(order.getCreatedAt().format(FORMATTER));

        // 转换时间线
        List<OrderVO.TimelineVO> timelineVOs = timelines.stream()
                .sorted(Comparator.comparing(OrderTimeline::getTime))
                .map(t -> {
                    OrderVO.TimelineVO tvo = new OrderVO.TimelineVO();
                    tvo.setT(t.getTime().format(FORMATTER));
                    tvo.setTitle(t.getTitle());
                    tvo.setDesc(t.getDesc());
                    tvo.setStatus(t.getStatus());
                    // 解析照片列表
                    if (t.getPhotos() != null && !t.getPhotos().isEmpty()) {
                        tvo.setPhotos(JSON.parseArray(t.getPhotos(), String.class));
                    }
                    return tvo;
                })
                .collect(Collectors.toList());
        vo.setTimeline(timelineVOs);

        return vo;
    }
}
