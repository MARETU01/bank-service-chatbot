package com.maretu.api.client;

import com.maretu.api.dto.UserDTO;
import com.maretu.common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "user-service")
public interface UserClient {

    @PostMapping("/users/pay-password/verify")
    Result<Boolean> verifyPayPassword(@RequestHeader("user-info") String userJson, @RequestBody String payPassword);
}
