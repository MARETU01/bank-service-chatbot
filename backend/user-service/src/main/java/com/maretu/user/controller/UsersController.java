package com.maretu.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.common.utils.Result;
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
    public Result<Boolean> sendCode(@RequestBody Users user,
                                    @RequestParam String type) {
        try {
            return Result.success(usersService.sendCode(user, type));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
