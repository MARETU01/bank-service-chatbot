package com.maretu.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDTO {

    private Integer userId;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String avatarUrl;

    private String accountStatus;
}
