package com.easy.id.service;

import java.util.Set;

public interface EasyIdService {

    Long getNextId(String businessType);

    Set<Long> getNextIdBatch(String businessType, int batchSize);
}
