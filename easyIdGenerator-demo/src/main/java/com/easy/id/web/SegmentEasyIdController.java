package com.easy.id.web;

import com.easy.id.service.segment.SegmentEasyIdService;
import com.easy.id.web.resp.ApiResponse;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
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
public class SegmentEasyIdController {

    @Autowired
    private SegmentEasyIdService easyIdService;

    @GetMapping("/next_id")
    public ApiResponse<String> getNextId(@NotNull(message = "业务类型必填") String businessType) {
        return ApiResponse.data(easyIdService.getNextId(businessType).toString());
    }

    @GetMapping("/next_id/batches")
    public ApiResponse<Set<String>> getNextId(@Range(message = "批量范围为1-1000", max = 1000, min = 1) Integer batchSize,
                                              @NotNull(message = "业务类型必填") String businessType) {
        return ApiResponse.data(easyIdService.getNextIdBatch(businessType, batchSize).stream()
                .map(Object::toString).collect(Collectors.toSet()));
    }
}
