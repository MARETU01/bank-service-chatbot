package com.maretu.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PageDTO<T> {

    private List<T> data;

    // 总记录数
    private Long total;

    // 总页数
    private Long totalPage;
}
