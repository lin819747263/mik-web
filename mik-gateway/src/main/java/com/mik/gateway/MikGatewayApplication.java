package com.mik.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.mik.user.api")
public class MikGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MikGatewayApplication.class, args);
    }
}
