package com.easy.id;

import com.easy.id.service.SegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年05月29日
 */
@RestController
@RequestMapping("/ids")
@Validated
public class EasyIdController {

    @Autowired
    private SegmentService segmentService;

    // todo做检验
    @GetMapping("/next_id")
    public Long getNextId(@NotEmpty String businessType) {
        return segmentService.getNextSegment(businessType).getId();
    }
}
