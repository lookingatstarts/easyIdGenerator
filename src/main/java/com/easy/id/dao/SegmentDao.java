package com.easy.id.dao;

import com.easy.id.entity.Segment;

public interface SegmentDao {

    Segment findByBusinessType(String businessType);

    int updateSegment();
}
