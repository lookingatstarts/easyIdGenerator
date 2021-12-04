package com.easy.id.web;

import com.easy.id.service.snowflake.SnowflakeEasyIdService;
import com.easy.id.web.resp.ApiResponse;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年05月29日
 */
@RestController
@RequestMapping("/snowflake/ids")
@Validated
@ConditionalOnBean(SnowflakeEasyIdService.class)
public class SnowflakeEasyIdController {

    @Autowired
    private SnowflakeEasyIdService easyIdService;

    @GetMapping("/next_id")
    public ApiResponse<String> getNextId() {
        return ApiResponse.data(easyIdService.getNextId(null).toString());
    }

    @GetMapping("/next_id/batches")
    public ApiResponse<Set<String>> getNextId(@Range(message = "批量范围为1-1000", max = 1000, min = 1) Integer batchSize) {
        return ApiResponse.data(easyIdService.getNextIdBatch(null, batchSize).stream()
                .map(Object::toString).collect(Collectors.toSet()));
    }
}
