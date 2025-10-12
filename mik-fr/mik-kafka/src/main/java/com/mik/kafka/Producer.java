package com.mik.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@Component
public class Producer {

    Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

//    @Scheduled(fixedRate = 1000)
    public void send() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("kafka-test",
                new Stdent().setAge(22).setName("科华").setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        kafkaTemplate.send("kafka-test", new Stdent()
                .setAge(22).setName("科华")
                .setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))).get(1000, TimeUnit.MILLISECONDS);

        kafkaTemplate.send("kafka-test", new Stdent()
                .setAge(22).setName("科华")
                .setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("消息发送成功:" + result.toString());
            } else {
                log.info("消息发送失败:" + ex.getMessage());
            }
        });

        SendResult<?, ?> sendResult = future.get(500, TimeUnit.MILLISECONDS);
        log.info("消息发送成功:" + sendResult.toString());
    }
}
