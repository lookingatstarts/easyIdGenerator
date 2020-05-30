package com.easy.id.service;

import com.easy.id.generator.IdGeneratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class EasyIdServiceImpl implements EasyIdService {

    @Autowired
    private IdGeneratorFactory idGeneratorFactory;

    @Override
    public Long getNextId(String businessType) {
        return idGeneratorFactory.getIdGenerator(businessType).nextId();
    }

    @Override
    public Set<Long> getNextIdBatch(String businessType, int batchSize) {
        return idGeneratorFactory.getIdGenerator(businessType).nextIds(batchSize);
    }
}
