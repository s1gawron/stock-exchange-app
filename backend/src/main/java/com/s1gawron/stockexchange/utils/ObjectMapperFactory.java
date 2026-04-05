package com.s1gawron.stockexchange.utils;

import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

public enum ObjectMapperFactory {

    I;

    private JsonMapper.Builder getMapperBuilder() {
        return JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
    }

    public ObjectMapper getMapper() {
        return getMapperBuilder().build();
    }

    public ObjectMapper getMapperForCache() {
        final BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.s1gawron.stockexchange.")
            .allowIfSubType("java.util.")
            .allowIfSubType("java.time.")
            .allowIfSubType("java.math.")
            .build();

        return getMapperBuilder()
            .activateDefaultTyping(typeValidator, DefaultTyping.NON_FINAL_AND_RECORDS)
            .build();
    }

}
