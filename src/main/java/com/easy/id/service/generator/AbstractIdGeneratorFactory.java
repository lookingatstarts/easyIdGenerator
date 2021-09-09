package com.easy.id.service.generator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractIdGeneratorFactory implements IdGeneratorFactory {

    private final Map<String, IdGenerator> idGeneratorMap = new ConcurrentHashMap<>();

    /**
     * 使用模版设计模式，目的时让子类复用父类获取IdGenerator的固定逻辑
     */
    @Override
    public IdGenerator getIdGenerator(String businessType) {
        // 双重判断
        if (idGeneratorMap.containsKey(businessType)) {
            return idGeneratorMap.get(businessType);
        }
        synchronized (this) {
            if (idGeneratorMap.containsKey(businessType)) {
                return idGeneratorMap.get(businessType);
            }
            IdGenerator idGenerator = createIdGenerator(businessType);
            idGeneratorMap.put(businessType, idGenerator);
            return idGenerator;
        }
    }

    protected abstract IdGenerator createIdGenerator(String businessType);

}
