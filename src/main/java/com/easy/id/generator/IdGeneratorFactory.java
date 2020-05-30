package com.easy.id.generator;

public interface IdGeneratorFactory {

    IdGenerator getIdGenerator(String businessType);
}
