package com.mik.rockmq;


import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class Terst {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    @Scheduled(fixedRate = 5000)
    public void send() throws MQClientException {
        System.out.println("执行了");
//        rocketMQTemplate.getProducer().setCreateTopicKey("AUTO_CREATE_TOPIC_KEY");
//        rocketMQTemplate.getProducer().createTopic("135135", "kh666", 1);
        SendResult sendResult = rocketMQTemplate.syncSend("TestTopic", new GenericMessage<String>("kesdcfsdfsd"));
        System.out.println(sendResult.toString());
    }

}
