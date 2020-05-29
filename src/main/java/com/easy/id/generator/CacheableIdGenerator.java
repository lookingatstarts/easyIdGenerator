package com.easy.id.generator;

import com.easy.id.entity.Segment;
import com.easy.id.service.SegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class CacheableIdGenerator implements IdGenerator {

    private final String businessType;
    private volatile Segment current;
    private volatile Segment next;
    @Autowired
    private SegmentService segmentService;

    @Override
    public Long nextId() {
        return null;
    }

    @Override
    public Set<Long> nextIds(int patchSize) {
        if (patchSize == 1) {
            return Collections.singleton(nextId());
        }
        Set<Long> ids = new HashSet<>(patchSize);
        for (int i = 0; i < patchSize; i++) {
            ids.add(nextId());
        }
        return ids;
    }
}
