package com.maretu.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 用户服务启动入口
 */
@SpringBootApplication(scanBasePackages = {"com.maretu.user", "com.maretu.common.config"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.maretu.api.client")
@MapperScan("com.maretu.user.mapper")
@EnableAsync
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
