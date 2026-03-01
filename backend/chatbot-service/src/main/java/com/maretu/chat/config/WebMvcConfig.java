package com.maretu.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类
 * 配置异步请求处理的线程池
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 使用SimpleAsyncTaskExecutor并启用虚拟线程
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setVirtualThreads(true);
        configurer.setTaskExecutor(taskExecutor);
        // 设置异步请求超时时间（可选，默认30秒）
        configurer.setDefaultTimeout(60000);
    }
}
