package com.easy.id.generator;

import com.easy.id.service.SegmentIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(value = IdGeneratorFactory.class)
public class DefaultIdGeneratorFactory extends AbstractIdGeneratorFactory {

    @Autowired
    private SegmentIdService segmentIdService;

    @Override
    protected IdGenerator createIdGenerator(String businessType) {
        return new CachedIdGenerator(businessType, segmentIdService);
    }
}
