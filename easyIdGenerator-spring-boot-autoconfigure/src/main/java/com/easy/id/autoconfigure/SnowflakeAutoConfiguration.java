package com.easy.id.autoconfigure;


import com.easy.id.service.snowflake.Snowflake;
import com.easy.id.service.snowflake.SnowflakeEasyIdService;
import com.easy.id.service.snowflake.SnowflakeZKHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(SnowflakeProperties.class)
@ConditionalOnProperty(prefix = "easy-id-generator.snowflake", value = "enable", havingValue = "true")
public class SnowflakeAutoConfiguration {

    @Autowired
    private SnowflakeProperties properties;

    public SnowflakeAutoConfiguration() {
        log.info("启动雪花算法生成ID");
    }

    @Bean
    public SnowflakeZKHolder snowflakeZKHolder() {
        SnowflakeZKHolder snowflakeZKHolder = new SnowflakeZKHolder(properties);
        snowflakeZKHolder.postConstruct();
        return snowflakeZKHolder;
    }

    @Bean
    public Snowflake snowflake(SnowflakeZKHolder snowflakeZKHolder) {
        return new Snowflake(snowflakeZKHolder.getWorkerID());
    }

    @Bean
    public SnowflakeEasyIdService snowflakeEasyIdService(Snowflake snowflake) {
        return new SnowflakeEasyIdService(snowflake);
    }
}
