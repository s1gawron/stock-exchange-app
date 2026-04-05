package com.s1gawron.stockexchange.configuration;

import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ObjectMapperConfiguration {

    @Bean
    public ObjectMapper defaultObjectMapper() {
        return ObjectMapperFactory.I.getMapper();
    }

    @Bean
    public ObjectMapper cacheObjectMapper() {
        return ObjectMapperFactory.I.getMapperForCache();
    }

}
