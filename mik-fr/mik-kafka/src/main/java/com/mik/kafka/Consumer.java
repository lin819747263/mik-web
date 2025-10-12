package com.mik.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class Consumer {

    Logger log = Logger.getLogger(this.getClass().getName());

    @KafkaListener(topics = {"kafka-test"})
    public void consume(String message) {
        log.info("接受到消息:" + message);

        log.info("接受到消息:" + message);
    }

}
