package com.easy.id.service.segment;

import com.easy.id.autoconfigure.SegmentConfiguration;
import com.easy.id.service.generator.AbstractIdGeneratorFactory;
import com.easy.id.service.generator.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * 生成IdGenerator的工厂
 */
@Component
@ConditionalOnBean(SegmentConfiguration.class)
public class SegmentIdGeneratorFactory extends AbstractIdGeneratorFactory {

    @Autowired
    private SegmentIdService segmentIdService;

    @Autowired
    @Qualifier(value = "fetchNextSegmentExecutor")
    private ExecutorService fetchNextSegmentExecutor;

    @Override
    protected IdGenerator createIdGenerator(String businessType) {
        return new SegmentCachedIdGenerator(fetchNextSegmentExecutor, segmentIdService, businessType);
    }
}
