package com.mik.core.util;

import lombok.Getter;

public class ObjectMapper {
    @Getter
    private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

}
