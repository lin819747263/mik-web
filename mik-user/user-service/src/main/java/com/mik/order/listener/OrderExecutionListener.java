package com.mik.order.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 工单流程执行监听器
 * 处理流程开始、结束等事件
 */
@Slf4j
@Component("orderExecutionListener")
public class OrderExecutionListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
        String eventName = execution.getEventName();
        String processInstanceId = execution.getProcessInstanceId();
        String processBusinessKey = execution.getProcessInstanceBusinessKey();

        log.info("流程事件: eventName={}, processInstanceId={}, businessKey={}",
                eventName, processInstanceId, processBusinessKey);

        if ("start".equals(eventName)) {
            handleStart(execution);
        } else if ("end".equals(eventName)) {
            handleEnd(execution);
        }
    }

    /**
     * 流程开始事件
     */
    private void handleStart(DelegateExecution execution) {
        log.info("工单流程开始, orderId={}", execution.getVariable("orderId"));
    }

    /**
     * 流程结束事件
     */
    private void handleEnd(DelegateExecution execution) {
        log.info("工单流程结束, orderId={}", execution.getVariable("orderId"));
        // 可以在这里触发通知等后续操作
    }
}
