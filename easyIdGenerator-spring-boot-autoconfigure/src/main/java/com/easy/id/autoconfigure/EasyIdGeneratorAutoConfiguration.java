package com.easy.id.autoconfigure;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration(value = {SegmentConfiguration.class, SnowflakeConfiguration.class})
public class EasyIdGeneratorAutoConfiguration {

}
