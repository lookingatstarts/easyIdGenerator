package com.easy.id.service.segment;

import com.easy.id.service.EasyIdService;
import com.easy.id.service.generator.IdGeneratorFactory;

import java.util.Set;

public class SegmentEasyIdService implements EasyIdService {

    private final IdGeneratorFactory idGeneratorFactory;

    public SegmentEasyIdService(IdGeneratorFactory factory) {
        this.idGeneratorFactory = factory;
    }

    @Override
    public Long getNextId(String businessType) {
        return idGeneratorFactory.getIdGenerator(businessType).nextId();
    }

    @Override
    public Set<Long> getNextIdBatch(String businessType, int batchSize) {
        return idGeneratorFactory.getIdGenerator(businessType).nextIds(batchSize);
    }
}
