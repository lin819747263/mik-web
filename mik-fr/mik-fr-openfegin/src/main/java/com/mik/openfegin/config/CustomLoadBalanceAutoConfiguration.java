package com.mik.openfegin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Configuration;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

/**
 * 自定义负载均衡自动配置
 *
 */
@Configuration
@LoadBalancerClients(defaultConfiguration = VersionBasedLoadBalancerConfiguration.class)
public class CustomLoadBalanceAutoConfiguration {

}
