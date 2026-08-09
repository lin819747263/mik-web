package com.mik.order.service;

import com.mik.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工单流程服务
 * 封装 Flowable 流程引擎操作
 */
@Slf4j
@Service
public class OrderFlowService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    /**
     * 启动工单流程
     *
     * @param orderId   工单ID
     * @param orderNo   工单编号
     * @param creatorId 创建人ID
     * @return 流程实例ID
     */
    public String startOrderProcess(Long orderId, String orderNo, Long creatorId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderId", orderId);
        variables.put("orderNo", orderNo);
        variables.put("creatorId", creatorId);

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                "order-process", orderNo, variables);

        log.info("工单流程已启动, orderNo={}, processInstanceId={}", orderNo, instance.getId());
        return instance.getId();
    }

    /**
     * 受理工单
     *
     * @param processInstanceId 流程实例ID
     * @param operatorId        受理人ID
     * @param approved          是否受理
     * @param desc              备注
     */
    public void acceptOrder(String processInstanceId, Long operatorId, boolean approved, String desc) {
        Task task = getTaskByDefinitionKey(processInstanceId, "acceptTask");

        Map<String, Object> variables = new HashMap<>();
        variables.put("operatorId", operatorId);
        variables.put("approved", approved);
        variables.put("acceptDesc", desc);

        taskService.complete(task.getId(), variables);
        log.info("工单已受理, processInstanceId={}, approved={}", processInstanceId, approved);
    }

    /**
     * 指派工单
     *
     * @param processInstanceId 流程实例ID
     * @param assignerId        指派人ID
     * @param assigneeId        处理人ID
     * @param deptId            部门ID
     * @param desc              备注
     */
    public void assignOrder(String processInstanceId, Long assignerId,
                           Long assigneeId, Long deptId, String desc) {
        Task task = getTaskByDefinitionKey(processInstanceId, "assignTask");

        Map<String, Object> variables = new HashMap<>();
        variables.put("assignerId", assignerId);
        variables.put("assigneeId", assigneeId);
        variables.put("deptId", deptId);
        variables.put("assignDesc", desc);

        taskService.complete(task.getId(), variables);
        log.info("工单已指派, processInstanceId={}, assigneeId={}", processInstanceId, assigneeId);
    }

    /**
     * 审核工单
     *
     * @param processInstanceId 流程实例ID
     * @param reviewerId        审核人ID
     * @param approved          是否通过
     * @param rejectTarget      退回目标（assign-退回指派, process-退回处理）
     * @param desc              审核意见
     */
    public void reviewOrder(String processInstanceId, Long reviewerId,
                           boolean approved, String rejectTarget, String desc) {
        Task task = getTaskByDefinitionKey(processInstanceId, "reviewTask");

        Map<String, Object> variables = new HashMap<>();
        variables.put("reviewerId", reviewerId);
        variables.put("approved", approved);
        variables.put("rejectTarget", rejectTarget != null ? rejectTarget : "assign");
        variables.put("reviewDesc", desc);

        taskService.complete(task.getId(), variables);
        log.info("工单已审核, processInstanceId={}, approved={}", processInstanceId, approved);
    }

    /**
     * 获取当前任务
     *
     * @param processInstanceId 流程实例ID
     * @return 当前任务
     */
    public Task getCurrentTask(String processInstanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    /**
     * 获取用户待办任务
     *
     * @param userId 用户ID
     * @return 待办任务列表
     */
    public List<Task> getUserTasks(Long userId) {
        return taskService.createTaskQuery()
                .taskAssignee(String.valueOf(userId))
                .orderByTaskCreateTime().desc()
                .list();
    }

    /**
     * 获取用户所在组的任务
     *
     * @param groups 用户组列表
     * @return 任务列表
     */
    public List<Task> getGroupTasks(List<String> groups) {
        return taskService.createTaskQuery()
                .taskCandidateGroupIn(groups)
                .orderByTaskCreateTime().desc()
                .list();
    }

    /**
     * 认领任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    public void claimTask(String taskId, Long userId) {
        taskService.claim(taskId, String.valueOf(userId));
    }

    /**
     * 根据任务定义Key获取任务
     */
    private Task getTaskByDefinitionKey(String processInstanceId, String taskDefinitionKey) {
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(taskDefinitionKey)
                .singleResult();

        if (task == null) {
            throw new ServiceException("当前没有待处理的任务");
        }

        return task;
    }

    /**
     * 删除流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param reason            原因
     */
    public void deleteProcess(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("流程实例已删除, processInstanceId={}, reason={}", processInstanceId, reason);
    }
}
