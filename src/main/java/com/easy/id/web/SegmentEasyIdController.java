package com.easy.id.web;

import com.easy.id.config.Module;
import com.easy.id.service.EasyIdService;
import com.easy.id.web.resp.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年05月29日
 */
@RestController
@RequestMapping("/segment/ids")
@Validated
@Module(value = "segment.enable")
public class SegmentEasyIdController {

    @Autowired
    @Qualifier("segmentEasyIdService")
    private EasyIdService easyIdService;

    @GetMapping("/next_id")
    public ApiResponse<String> getNextId(@NotEmpty String businessType) {
        return ApiResponse.data(easyIdService.getNextId(businessType).toString());
    }

    @GetMapping("/next_id/batches")
    public ApiResponse<Set<String>> getNextId(@RequestParam(value = "batches_size", defaultValue = "100")
                                              @Min(value = 0, message = "批量生成数必须大于0")
                                              @Max(value = 1000, message = "最大支持批量生成数为1000") Integer batchSize,
                                              @NotEmpty String businessType) {
        return ApiResponse.data(easyIdService.getNextIdBatch(businessType, batchSize).stream()
                .map(Object::toString).collect(Collectors.toSet()));
    }
}
