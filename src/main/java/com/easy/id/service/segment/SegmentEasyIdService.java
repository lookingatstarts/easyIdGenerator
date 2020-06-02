package com.easy.id.service.segment;

import com.easy.id.config.Module;
import com.easy.id.service.EasyIdService;
import com.easy.id.service.generator.IdGeneratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Module(value = "segment.enable")
public class SegmentEasyIdService implements EasyIdService {

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
