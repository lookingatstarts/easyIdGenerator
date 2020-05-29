package com.easy.id.service.impl;

import com.easy.id.dao.SegmentDao;
import com.easy.id.entity.Segment;
import com.easy.id.service.SegmentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SegmentServiceImpl implements SegmentService {

    @Autowired
    private SegmentDao segmentDao;

    @Override
    public Segment getNextSegment(String businessType) {
        return segmentDao.selectByBusinessType(businessType);
    }
}
