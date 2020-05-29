package com.easy.id.dao;

import com.easy.id.entity.Segment;

public interface SegmentDao {

    Segment selectByBusinessType(String businessType);
}