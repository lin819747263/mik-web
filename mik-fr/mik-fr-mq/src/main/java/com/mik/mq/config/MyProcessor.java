package com.mik.mq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class MyProcessor {

//    @Bean
//    public Supplier<Message<String>> send() {
//        System.out.println("Send message: ");
//        return () -> MessageBuilder.withPayload("Hello, World!").build();
//    }

    // 定义一个消息处理函数，用于接收和处理消息
    @Bean
    public Function<Message<String>, String> receive() {
        return message -> {
            System.out.println("Received message: " + message.getPayload());
            return message.getPayload().toUpperCase();
        };
    }
}
