package com.easy.id.web;

import com.easy.id.config.Module;
import com.easy.id.service.EasyIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年05月29日
 */
@RestController
@RequestMapping("/snowflake/ids")
@Validated
@Module(value = "snowflake.enable")
public class SnowflakeEasyIdController {

    @Autowired
    @Qualifier("snowflakeEasyIdService")
    private EasyIdService easyIdService;

    @GetMapping("/next_id")
    public Long getNextId() {
        return easyIdService.getNextId(null);
    }

    @GetMapping("/next_id/batches")
    public Set<Long> getNextId(@RequestParam(value = "batches_size", defaultValue = "100") Integer batchSize) {
        return easyIdService.getNextIdBatch(null, batchSize);
    }
}
