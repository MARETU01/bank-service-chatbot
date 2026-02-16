package com.maretu.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 聊天机器人服务启动入口
 */
@SpringBootApplication(scanBasePackages = {"com.maretu.chat", "com.maretu.common.config"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.maretu.api.client")
@MapperScan("com.maretu.chat.mapper")
@EnableAsync
public class ChatbotServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatbotServiceApplication.class, args);
    }
}
