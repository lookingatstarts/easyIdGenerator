package com.easy.id.service;

import com.easy.id.entity.Segment;

public interface SegmentService {

    Segment getNextSegment(String businessType);
}
