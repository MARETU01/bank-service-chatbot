package com.maretu.api.client;

import com.maretu.api.dto.UserDTO;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient(value = "user-service")
public interface UserClient {

    @RequestLine("GET /users/{id}")
    UserDTO getUser(@Param("id") Long id);
}
