package com.mik.rockmq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(consumerGroup = "66666", topic = "TestTopic")
public class Consumer02 implements RocketMQListener<String> {

    @Override
    public void onMessage(String s) {
        System.out.println("接受到消息：" + s);
    }
}
