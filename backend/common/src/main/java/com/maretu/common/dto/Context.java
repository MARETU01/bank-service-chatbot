package com.maretu.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Context {
    private Integer userId;

    private String username;

    private String email;

    private String ip;
}
