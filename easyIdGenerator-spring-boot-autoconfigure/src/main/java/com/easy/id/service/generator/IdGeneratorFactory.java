package com.easy.id.service.generator;

public interface IdGeneratorFactory {

    IdGenerator getIdGenerator(String businessType);
}
