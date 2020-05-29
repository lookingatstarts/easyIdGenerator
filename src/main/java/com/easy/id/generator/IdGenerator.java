package com.easy.id.generator;

import java.util.Set;

/**
 * id 生成器
 */
public interface IdGenerator {

    Long nextId();

    Set<Long> nextIds(int patchSize);
}
