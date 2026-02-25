package com.maretu.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.common.utils.Result;
import com.maretu.user.dto.ResetPasswordReq;
import com.maretu.user.dto.UpdateProfileReq;
import com.maretu.user.pojo.Users;
import com.maretu.user.service.IUsersService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final IUsersService usersService;
    private final ObjectMapper jacksonObjectMapper;

    public UsersController(IUsersService usersService, ObjectMapper jacksonObjectMapper) {
        this.usersService = usersService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

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
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        if (!user.getLastLoginIp().equals(ip)) {
            return Result.failure("IP address mismatch");
        }
        try {
            return Result.success(usersService.refresh(user, ip));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/me")
    public Result<Users> getCurrentUser(@RequestHeader("user-info") String userJson,
                                        @RequestHeader("X-Client-IP") String ip) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        if (!user.getLastLoginIp().equals(ip)) {
            return Result.failure("IP address mismatch");
        }
        try {
            return Result.success(usersService.getCurrentUser(user));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result<Users> updateProfile(@RequestHeader("user-info") String userJson,
                                       @RequestHeader("X-Client-IP") String ip,
                                       @RequestBody UpdateProfileReq req) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        if (!user.getLastLoginIp().equals(ip)) {
            return Result.failure("IP address mismatch");
        }
        try {
            return Result.success(usersService.updateProfile(user, req));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
