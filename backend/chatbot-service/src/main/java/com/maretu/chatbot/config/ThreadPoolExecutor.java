package com.maretu.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolExecutor {

    @Bean(name = "virtualThreadPoolExecutor")
    public Executor virtualThreadPoolExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
