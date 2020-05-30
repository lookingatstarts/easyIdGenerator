package com.easy.id.service;

import com.easy.id.dao.SegmentDao;
import com.easy.id.dao.entity.Segment;
import com.easy.id.exception.FetchSegmentFailException;
import com.easy.id.exception.SegmentNotFoundException;
import com.easy.id.generator.SegmentId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SegmentServiceImpl implements SegmentIdService {

    @Autowired
    private SegmentDao segmentDao;

    @Value("${easy-id.fetch-segment-retry-times:2")
    private int retry;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SegmentId fetchNextSegmentId(String businessType) {
        // 获取segment的时候，有可能存在version冲突，需要重试
        for (int i = 0; i < retry; i++) {
            final Segment segment = segmentDao.selectByBusinessType(businessType);
            if (segment == null) {
                throw new SegmentNotFoundException("can not find segment of " + businessType);
            }
            int update = segmentDao.updateSegmentMaxId(segment.getId(), segment.getMaxId() + segment.getStep(), segment.getVersion());
            if (update == 1) {
                log.debug("fetch {} next segment {} success", businessType, segment.toString());
                return new SegmentId(segment);
            }
            log.info("fetch {} next segment {} conflict,retry", businessType, segment.toString());
        }
        throw new FetchSegmentFailException("fetch " + businessType + " next segment fail");
    }
}
