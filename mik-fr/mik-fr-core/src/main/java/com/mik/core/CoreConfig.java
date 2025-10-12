package com.mik.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CoreConfig {

    @Bean
    @Order(1)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
