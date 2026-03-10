package com.maretu.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import com.maretu.user.dto.PayPasswordReq;
import com.maretu.user.dto.ResetPasswordReq;
import com.maretu.user.dto.UpdateProfileReq;
import com.maretu.user.dto.VerifyPayPasswordReq;
import com.maretu.user.pojo.Users;
import com.maretu.user.service.IUserSecurityService;
import com.maretu.user.service.IUsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UsersController {

    private final IUsersService usersService;
    private final IUserSecurityService userSecurityService;
    private final ObjectMapper jacksonObjectMapper;

    @PostMapping("/login")
    public Result<String> login(@RequestHeader("X-Client-IP") String ip,
                                @RequestBody Users user) {
        try {
            return Result.success(usersService.login(user, ip));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/code")
    public Result<Boolean> sendCode(@RequestBody(required = false) Users user,
                                    @RequestHeader(value = "user-info", required = false) String userJson,
                                    @RequestParam("type") String type) throws JsonProcessingException {
        if (userJson != null && !userJson.isEmpty()) {
            user.setEmail(jacksonObjectMapper.readValue(userJson, Users.class).getEmail());
        }
        try {
            return Result.success(usersService.sendCode(user, type));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody Users user,
                                    @RequestParam("verifyCode") String verifyCode) {
        try {
            return Result.success(usersService.register(user, verifyCode));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public Result<Boolean> resetPassword(@RequestHeader(value = "user-info", required = false) String userJson,
                                         @RequestBody(required = false) ResetPasswordReq req,
                                         @RequestParam("verifyCode") String verifyCode) throws JsonProcessingException {
        if (userJson != null && !userJson.isEmpty()) {
            req.setEmail(jacksonObjectMapper.readValue(userJson, Users.class).getEmail());
        }
        try {
            return Result.success(usersService.resetPassword(req, verifyCode));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public Result<String> refreshToken(@RequestHeader("user-info") String userJson,
                                       @RequestHeader("X-Client-IP") String ip) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        if (!context.getIp().equals(ip)) {
            return Result.failure("IP address mismatch");
        }
        try {
            return Result.success(usersService.refresh(context.getUserId(), ip));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/me")
    public Result<Users> getCurrentUser(@RequestHeader("user-info") String userJson,
                                        @RequestHeader("X-Client-IP") String ip) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        if (!context.getIp().equals(ip)) {
            return Result.failure("IP address mismatch");
        }
        try {
            return Result.success(usersService.getCurrentUser(context.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result<Users> updateProfile(@RequestHeader("user-info") String userJson,
                                       @RequestHeader("X-Client-IP") String ip,
                                       @RequestBody UpdateProfileReq req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        if (!context.getIp().equals(ip)) {
            return Result.failure("IP address mismatch");
        }
        try {
            return Result.success(usersService.updateProfile(context.getUserId(), req));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/pay-password")
    public Result<Boolean> setPayPassword(@RequestHeader("user-info") String userJson,
                                          @RequestBody PayPasswordReq req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            userSecurityService.setPayPassword(Long.valueOf(context.getUserId()), req.getPayPassword());
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping("/pay-password")
    public Result<Boolean> updatePayPassword(@RequestHeader("user-info") String userJson,
                                             @RequestBody PayPasswordReq req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            userSecurityService.updatePayPassword(
                    Long.valueOf(context.getUserId()),
                    req.getOldPayPassword(),
                    req.getPayPassword()
            );
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/pay-password/verify")
    public Result<Boolean> verifyPayPassword(@RequestHeader("user-info") String userJson,
                                             @RequestBody VerifyPayPasswordReq req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            boolean result = userSecurityService.verifyPayPassword(
                    Long.valueOf(context.getUserId()),
                    req.getPayPassword()
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/pay-password/status")
    public Result<Boolean> getPayPasswordStatus(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(userSecurityService.hasPayPassword(Long.valueOf(context.getUserId())));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
