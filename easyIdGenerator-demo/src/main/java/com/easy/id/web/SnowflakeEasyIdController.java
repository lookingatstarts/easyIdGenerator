package com.easy.id.web;

import com.easy.id.service.snowflake.SnowflakeEasyIdService;
import com.easy.id.web.resp.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
public class SnowflakeEasyIdController {

    @Autowired
    private SnowflakeEasyIdService easyIdService;

    public SnowflakeEasyIdController() {
        System.out.println("dddddddddddddddddddddddddddddddddddddddd");
    }

    @GetMapping("/next_id")
    public ApiResponse<String> getNextId() {
        return ApiResponse.data(easyIdService.getNextId(null).toString());
    }

    @GetMapping("/next_id/batches")
    public ApiResponse<Set<String>> getNextId(@RequestParam(value = "batches_size", defaultValue = "100")
                                              @Min(value = 0, message = "批量生成数必须大于0")
                                              @Max(value = 1000, message = "最大支持批量生成数为1000") Integer batchSize) {
        return ApiResponse.data(easyIdService.getNextIdBatch(null, batchSize).stream()
                .map(Object::toString).collect(Collectors.toSet()));
    }
}
