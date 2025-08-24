package com.codewithsai.alert.notification.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();


    public static String toJsonString(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }
}

