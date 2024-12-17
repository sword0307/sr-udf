package com.starunion.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonExistPathUDF {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Boolean evaluate(String jsonArray, String name, Integer checkType, Boolean sp) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        try {
            switch (checkType) {
                case 1:
                    // 校验路径
                    if (jsonArray == null || jsonArray.isEmpty()) {
                        return false;
                    }
                    return doesPathExistInAllElements(objectMapper.readTree(jsonArray), name.split("\\."));
                case 2:
                    // 校验空值
                    if (jsonArray == null || jsonArray.isEmpty()) {
                        return true;
                    }
                    return isAnyValueEmptyInPath(objectMapper.readTree(jsonArray), sp, name.split("\\."));
                default:
                    return false;
            }
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 检查指定 JSON 路径是否存在于所有数组元素中
     *
     * @param node         根节点
     * @param pathElements 路径元素
     * @return 指定路径是否存在于所有数组元素中
     */
    public static boolean doesPathExistInAllElements(JsonNode node, String... pathElements) {
        return checkPathExistence(node, 0, pathElements);
    }

    private static boolean checkPathExistence(JsonNode node, int index, String... pathElements) {
        if (index >= pathElements.length) {
            return true;
        }

        String element = pathElements[index];

        if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                if (!checkPathExistence(arrayElement, index, pathElements)) {
                    return false;
                }
            }
            return true;
        } else if (node.has(element)) {
            return checkPathExistence(node.get(element), index + 1, pathElements);
        }

        return false;
    }

    /**
     * 检查指定 JSON 路径上是否有任意值为空
     *
     * @param node         根节点
     * @param pathElements 路径元素
     * @return 指定路径上是否有任意值为空
     */
    public static boolean isAnyValueEmptyInPath(JsonNode node, Boolean sp, String... pathElements) {
        // 遍历整个JSON树，找到路径并检查值是否为空
        return checkPathForEmptyValue(node, 0, sp, pathElements);
    }

    private static boolean checkPathForEmptyValue(JsonNode node, int index, Boolean sp, String... pathElements) {
        if (index >= pathElements.length) {
            // 如果到达路径末尾，检查值是否为空
            return isNodeValueEmpty(node, sp);
        }

        String element = pathElements[index];
        if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                if (checkPathForEmptyValue(arrayElement, index, sp, pathElements)) {
                    return true;
                }
            }
        } else if (node.has(element)) {
            return checkPathForEmptyValue(node.get(element), index + 1, sp, pathElements);
        }
        return false;
    }


    private static boolean isNodeValueEmpty(JsonNode node, Boolean sp) {
        if (sp) {
            return node.isNull() || node.asText().isEmpty();
        } else {
            return node.isNull();
        }
    }

//    public static void main(String[] args) {
//        String jsonString = "[{ \"b\": [{ \"c\": \"value2\"}, { \"c\":\"\"}] }, { \"b\": [{ \"c\": \"value3\" }] }] ";
//        JsonExistPathUDF jsonExistPathUDF = new JsonExistPathUDF();
//        System.out.println(jsonExistPathUDF.evaluate(jsonString, "b.c", 2, true));
//    }
}