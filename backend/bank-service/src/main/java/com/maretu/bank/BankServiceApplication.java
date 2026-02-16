package com.maretu.bank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 银行服务启动入口
 */
@SpringBootApplication(scanBasePackages = {"com.maretu.bank", "com.maretu.common.config"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.maretu.api.client")
@MapperScan("com.maretu.bank.mapper")
@EnableAsync
public class BankServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankServiceApplication.class, args);
    }
}
