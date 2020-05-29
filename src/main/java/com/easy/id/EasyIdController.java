package com.easy.id;

import com.easy.id.service.SegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年05月29日
 */
@RestController
@RequestMapping("/easy-id/ids")
public class EasyIdController {

    @Autowired
    private SegmentService segmentService;

    @GetMapping
    public Long getNextId() {
        return segmentService.getNextSegment("test").getId();
    }
}
