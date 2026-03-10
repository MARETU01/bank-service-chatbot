package com.maretu.common.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private static final Integer SUCCESS_CODE = 1;
    private static final Integer FAILURE_CODE = 0;

    public Integer code;
    public String message;
    public T data;
    public Long timestamp;

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "success", null, System.currentTimeMillis());
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "success", data, System.currentTimeMillis());
    }

    public static <T> Result<T> failure() {
        return new Result<>(FAILURE_CODE, "failure", null, System.currentTimeMillis());
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(FAILURE_CODE, message, null, System.currentTimeMillis());
    }
}