package com.easy.id.entity;

import lombok.Data;

@Data
public class Result {

    private ResultCode code;
    private Long id;

    public Result(ResultCode code, Long id) {
        this.code = code;
        this.id = id;
    }
}
