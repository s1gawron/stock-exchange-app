package com.s1gawron.stockexchange.utils;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public enum ObjectMapperFactory {

    I;

    public ObjectMapper getMapper() {
        return JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build();
    }

}
