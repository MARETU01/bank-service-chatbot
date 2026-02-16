package com.maretu.api.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "chat-service")
public interface ChatClient {

}
