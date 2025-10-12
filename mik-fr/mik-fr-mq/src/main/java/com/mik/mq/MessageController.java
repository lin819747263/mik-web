package com.mik.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @Autowired
    private StreamBridge streamBridge;

    @GetMapping("/send")
    public String send() {
        streamBridge.send("send-out-0", "Hello, World!");
        return "Message sent!";
    }
}
