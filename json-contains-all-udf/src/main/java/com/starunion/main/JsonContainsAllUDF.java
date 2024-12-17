package com.starunion.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Iterator;

public class JsonContainsAllUDF {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Boolean evaluate(String jsonArray, String jsonObject) {
        if (jsonArray == null || jsonObject == null) {
            return false;
        }

        try {
            JsonNode arrayNode = objectMapper.readTree(jsonArray);
            JsonNode objectNode = objectMapper.readTree(jsonObject);

            if (!arrayNode.isArray()) {
                return false;
            }

            for (JsonNode element : arrayNode) {
                if (!containsAll(element, objectNode)) {
                    return false;
                }
            }

            return true;
        } catch (IOException e) {
            // 处理 JSON 解析错误
            return false;
        }
    }

    private boolean containsAll(JsonNode container, JsonNode contained) {
        Iterator<String> fieldNames = contained.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (!container.has(fieldName) || !container.get(fieldName).equals(contained.get(fieldName))) {
                return false;
            }
        }
        return true;
    }

}