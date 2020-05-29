package com.easy.id.service.impl;

import com.easy.id.dao.SegmentDao;
import com.easy.id.entity.Segment;
import com.easy.id.service.SegmentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SegmentServiceImpl implements SegmentService {

    private SegmentDao segmentDao;

    @Override
    public Segment getNextSegment(String businessType) {
        final Segment segment = segmentDao.findByBusinessType(businessType);
        return null;
    }
}
