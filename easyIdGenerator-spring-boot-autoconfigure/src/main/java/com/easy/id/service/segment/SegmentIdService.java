package com.easy.id.service.segment;

import com.easy.id.entity.SegmentId;

public interface SegmentIdService {

    SegmentId fetchNextSegmentId(String businessType);
}
