package com.starunion.main;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Pattern;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ObjectArrayFilterUDF {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ConditionFilter {
        private Integer Relation;

        @JsonProperty("conf_relation")
        public Integer getRelation() {
            return Relation;
        }

        @JsonProperty("conf_relation")
        public void setRelation(Integer relation) {
            Relation = relation;
        }

        @JsonProperty("indicator_conf_models")
        public List<Condition> getFilterConditions() {
            return FilterConditions;
        }

        @JsonProperty("indicator_conf_models")
        public void setFilterConditions(List<Condition> filterConditions) {
            FilterConditions = filterConditions;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Condition {
            @Override
            public String toString() {
                return "Condition{" + "FieldName='" + FieldName + '\'' + ", SubFieldType='" + SubFieldType + '\'' + ", FieldType='" + FieldType + '\'' + ", FieldKind=" + FieldKind + ", IntervalType=" + IntervalType + ", Values=" + Values + '}';
            }

            //字段名称 （当为虚拟属性时，对应虚拟属性字段名称）
            private String FieldName;
            //对象子属性字段类型 1：文本 2：数值 3：布尔 4：时间   6：用户分群  7.列表 8.对象 9. 对象组
            private String SubFieldType;
            //分组字段类型 1：文本 2：数值 3：布尔 4：时间 5.列表 6.对象 7. 对象组
            private String FieldType;
            //字段属性 1:事件属性 2:用户属性 3:系统属性 4：用户分群( 仅依赖参数grouping_model，field_name 可选填分群名称，用于前端回显) 5：维度表属性 6：虚拟属性 (绑定参数 virtual_attr)
            private Integer FieldKind;
            // 1-等于、2-大于、3-大于等于、4-小于、5小于等于、6-区间 闭区间 7：区间 左开右闭 8:区间 左闭右开 9:区间 全开区间 10:不等于 11：包含 12：不包含 13:为空 14：非空 15：正则匹配 16: 正则不匹配 17:有值 18：无值 19： true  20:false 21:时间类型筛选（闭区间）22.事件时间戳左闭右开
            private Integer IntervalType;
            //字段值(时间/bool/数值也转化为字符串)
            private List<String> Values;

            @JsonProperty("field_name")
            public String getFieldName() {
                return FieldName;
            }

            @JsonProperty("field_name")
            public void setFieldName(String fieldName) {
                FieldName = fieldName;
            }

            @JsonProperty("sub_field_type")
            public String getSubFieldType() {
                return SubFieldType;
            }

            @JsonProperty("sub_field_type")
            public void setSubFieldType(String subFieldType) {
                SubFieldType = subFieldType;
            }

            @JsonProperty("field_type")
            public String getFieldType() {
                return FieldType;
            }

            @JsonProperty("field_type")
            public void setFieldType(String fieldType) {
                FieldType = fieldType;
            }

            @JsonProperty("field_kind")
            public Integer getFieldKind() {
                return FieldKind;
            }

            @JsonProperty("field_kind")
            public void setFieldKind(Integer fieldKind) {
                FieldKind = fieldKind;
            }

            @JsonProperty("interval_type")
            public Integer getIntervalType() {
                return IntervalType;
            }

            @JsonProperty("interval_type")
            public void setIntervalType(Integer intervalType) {
                IntervalType = intervalType;
            }

            @JsonProperty("values")
            public List<String> getValues() {
                return Values;
            }

            @JsonProperty("values")
            public void setValues(List<String> values) {
                Values = values;
            }

        }

        @Override
        public String toString() {
            return "ConditionFilter{" + "Relation=" + Relation + ", FilterConditions=" + FilterConditions + '}';
        }

        private List<Condition> FilterConditions;

    }

    public Boolean evaluate(String jsonStr, String jsonPath, String conditions, Integer expectedResult) {
        if (jsonStr == null || jsonPath == null || conditions == null || expectedResult == null) {
            return false;
        }

        try {
            ConditionFilter ConditionFilter = objectMapper.readValue(conditions, ConditionFilter.class);
            JsonNode rootNode = objectMapper.readTree(jsonStr);
            ConditionFilter.setFilterConditions(ConditionFilter.getFilterConditions().stream().map(condition -> {
                condition.setFieldName(condition.getFieldName().substring(jsonPath.length() + 1));
                return condition;
            }).collect(Collectors.toList()));
            return ObjectArrayCheck(rootNode, jsonPath, ConditionFilter, expectedResult);
//            if (!jsonPath.contains(".")) {
//                ConditionFilter.setFilterConditions(ConditionFilter.getFilterConditions().stream()
//                        .map(condition -> {
//                            condition.setFieldName(condition.getFieldName().substring(jsonPath.length() + 1));
//                            return condition;
//                        })
//                        .collect(Collectors.toList()));
//                return ObjectArrayCheck(rootNode, ConditionFilter, expectedResult);
//            } else {
//                ConditionFilter.setFilterConditions(ConditionFilter.getFilterConditions().stream()
//                        .map(condition -> {
//                            condition.setFieldName(condition.getFieldName().substring(jsonPath.length() + 1));
//                            return condition;
//                        })
//                        .collect(Collectors.toList()));
//                JsonNode jsonNode = rootNode.get(jsonPath.substring(jsonPath.indexOf(".") + 1));
//                return ObjectArrayCheck(jsonNode, ConditionFilter, expectedResult);
//            }
        } catch (IOException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Error in condition: " + e.getMessage());
            return false;
        }
    }

    private boolean ObjectArrayCheck(JsonNode rootNode, String jsonPath, ConditionFilter conditionFilter, Integer expectedResult) throws JsonProcessingException {
        if (rootNode == null || rootNode.isNull()) {
            return false;
        }
//        String[] parts = jsonPath.split("\\.");
//        Integer level = parts.length;
        int size = 0;
        int satisfied = 0;
        if (rootNode.isArray()) {
            if (jsonPath.contains(".")) {
                for (JsonNode rNode : rootNode) {
                    if (rNode != null && !rNode.isNull()) {
                        JsonNode jsonNode = rNode.get(jsonPath.substring(jsonPath.indexOf(".") + 1));
                        if (jsonNode != null && !jsonNode.isNull()) {
                            if (jsonNode.isArray()) {
                                size = jsonNode.size();
                                satisfied = 0;
                                for (JsonNode node : jsonNode) {
                                    if (node != null && !node.isNull()) {
                                        if (ObjectCheck(node, conditionFilter)) {
                                            satisfied++;
                                        }
                                    }
                                }
                                if (judgeResult(expectedResult, size, satisfied)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            } else {
                size = rootNode.size();
                for (JsonNode node : rootNode) {
                    if (node != null && !node.isNull()) {
                        if (ObjectCheck(node, conditionFilter)) {
                            satisfied++;
                        }
                    }
                }
            }
        } else {
            if (jsonPath.contains(".")) {
                JsonNode jsonNode = rootNode.get(jsonPath.substring(jsonPath.indexOf(".") + 1));
                if (jsonNode != null && !jsonNode.isNull()) {
                    size = jsonNode.size();
                    for (JsonNode rNode : jsonNode) {
                        if (rNode != null && !rNode.isNull()) {
                            if (ObjectCheck(rNode, conditionFilter)) {
                                if (expectedResult == 3) {
                                    return false;
                                } else {
                                    return true;
                                }
                            }
                        }
                    }
                }
            } else {
                size = rootNode.size();
                for (JsonNode node : rootNode) {
                    if (node != null && !node.isNull()) {
                        if (ObjectCheck(node, conditionFilter)) {
                            satisfied++;
                        }
                    }
                }

            }
        }

        return judgeResult(expectedResult, size, satisfied);
    }

    private boolean judgeResult(Integer expectedResult, int size, int satisfied) {
        switch (expectedResult) {
            case 3:
                // 全部不满足
                return satisfied == 0;
            case 2:
                // 全部满足
                return (satisfied == size) && (satisfied > 0);
            case 1:
                // 部分满足
                return satisfied > 0;
            default:
                return false;
        }
    }


    private boolean ObjectCheck(JsonNode node, ConditionFilter conditionFilter) throws JsonProcessingException {
        List<ConditionFilter.Condition> filterConditions = conditionFilter.getFilterConditions();
        int size = filterConditions.size();
        int satisfied = 0;
        for (int i = 0; i < filterConditions.size(); i++) {
            ConditionFilter.Condition condition = filterConditions.get(i);
            JsonNode value = node.get(condition.FieldName);
            if (value == null) {
                if (condition.getIntervalType() == 13) {
                    satisfied++;
                }
                continue;
            }

            if (condition.getSubFieldType().equals("文本") || condition.getSubFieldType().equals("列表") || condition.getSubFieldType().equals("对象") || (condition.getSubFieldType().equals("对象组"))) {
                if (TextCheck(value, condition)) {
                    satisfied++;
                }
            } else if (condition.getSubFieldType().equals("数值")) {
                if (NumberCheck(value, condition)) {
                    satisfied++;
                }
            } else if (condition.getSubFieldType().equals("布尔")) {
                if (BooleanCheck(value, condition)) {
                    satisfied++;
                }
            }
        }
        if (conditionFilter.getRelation() == 1) {
            return (satisfied == size && satisfied > 0);
        } else {
            return (satisfied > 0);
        }

    }

    private boolean TextCheck(JsonNode node, ConditionFilter.Condition condition) throws JsonProcessingException {
        String str;
        if (condition.getSubFieldType().equals("列表") || condition.getSubFieldType().equals("对象") || (condition.getSubFieldType().equals("对象组"))) {
            str = objectMapper.writeValueAsString(node);
        } else {
            str = node.asText();
        }
        switch (condition.getIntervalType()) {
            case 1:
                //等于
                return isTextInArray(condition.getValues(), str);
            case 10:
                //不等于
                return !isTextInArray(condition.getValues(), str);
            case 11:
                //包含
                return str.contains(condition.getValues().get(0));
            case 12:
                //不包含
                return !str.contains(condition.getValues().get(0));
            case 13:
                //为空
                return node.isNull();
            case 14:
                //非空
                return !node.isNull();
            case 15:
                //正则匹配
                return Pattern.compile(condition.getValues().get(0)).matcher(str).find();
            case 16:
                //正则不匹配
                return !Pattern.compile(condition.getValues().get(0)).matcher(str).find();
            default:
                return false;
        }
    }


    private boolean NumberCheck(JsonNode node, ConditionFilter.Condition condition) {
        switch (condition.getIntervalType()) {
            case 1:
                //等于
                return isNumberInArray(convertStringArrayToDoubleArray(condition.getValues()), node.asDouble());
            case 10:
                //不等于
                return !isNumberInArray(convertStringArrayToDoubleArray(condition.getValues()), node.asDouble());
            case 2:
                //大于
                return node.asDouble() > Double.parseDouble(condition.getValues().get(0));
            case 4:
                //小于
                return node.asDouble() < Double.parseDouble(condition.getValues().get(0));
            case 3:
                //大于等于
                return node.asDouble() >= Double.parseDouble(condition.getValues().get(0));
            case 5:
                //小于等于
                return node.asDouble() <= Double.parseDouble(condition.getValues().get(0));
            case 6:
                //闭区间
                return node.asDouble() >= Double.parseDouble(condition.getValues().get(0)) && node.asDouble() <= Double.parseDouble(condition.getValues().get(1));
            case 9:
                //开区间
                return node.asDouble() > Double.parseDouble(condition.getValues().get(0)) && node.asDouble() < Double.parseDouble(condition.getValues().get(1));
            case 7:
                //左开右闭
                return node.asDouble() > Double.parseDouble(condition.getValues().get(0)) && node.asDouble() <= Double.parseDouble(condition.getValues().get(1));
            case 8:
                //左闭右开
                return node.asDouble() >= Double.parseDouble(condition.getValues().get(0)) && node.asDouble() < Double.parseDouble(condition.getValues().get(1));
            case 13:
                //为空
                return node.isNull();
            case 14:
                //非空
                return !node.isNull();
            default:
                return false;
        }

    }


    private boolean BooleanCheck(JsonNode node, ConditionFilter.Condition condition) {
        switch (condition.getIntervalType()) {
            case 13:
                //为空
                return node.isNull();
            case 14:
                //非空
                return !node.isNull();
            case 19:
                //为真
                return node.asBoolean();
            case 20:
                //为假
                return !node.asBoolean();
            default:
                return false;
        }
    }

    private static double[] convertStringArrayToDoubleArray(List<String> stringArray) {
        if (stringArray == null || stringArray.isEmpty()) {
            return new double[0];
        }

        double[] numberArray = new double[stringArray.size()];
        for (int i = 0; i < stringArray.size(); i++) {
            try {
                numberArray[i] = Double.parseDouble(stringArray.get(i));
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format: " + stringArray.get(i));
                numberArray[i] = Double.NaN; // 使用 NaN 表示无效值
            }
        }
        return numberArray;
    }


    private boolean isNumberInArray(double[] numberArray, double targetNumber) {
        for (double num : numberArray) {
            if (num == targetNumber) {
                return true;
            }
        }
        return false;
    }

    private boolean isTextInArray(List<String> array, String target) {
        for (String str : array) {
            if (str.equals(target)) {
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        ObjectArrayFilterUDF udf = new ObjectArrayFilterUDF();
        String s = "[{\"name\":\"褐甲迷猛蚁\",\"nickname\":\"褐甲迷猛蚁(jNCT)\",\"level\":7,\"is_boss\":false,\"skills\":[\"褐甲迷猛蚁002\",\"褐甲迷猛蚁007\",\"褐甲迷猛蚁009\"],\"info\":{\"defense\":45,\"grow\":\"{\\\"attack\\\":28,\\\"defense\\\":25}\",\"reward\":\"[{\\\"item\\\":\\\"1小时加速\\\",\\\"nums\\\":10},{\\\"item\\\":\\\"150k沙\\\",\\\"nums\\\":4},{\\\"nums\\\":14,\\\"item\\\":\\\"蚁国币\\\"},{\\\"nums\\\":1,\\\"item\\\":\\\"8小时加速\\\"},{\\\"nums\\\":1,\\\"item\\\":\\\"150k土\\\"}]\",\"chapter\":[\"5-7\",\"3-12\"],\"description\":\"冰霜洞穴的Boss艾尔文以其冰属性攻击闻名，技能“冰封领域”能使区域变得寒冷\",\"attack\":36}},{\"name\":\"死神蚁\",\"nickname\":\"死神蚁(TH7n)\",\"level\":9,\"is_boss\":false,\"skills\":[\"死神蚁003\",\"死神蚁009\",\"死神蚁003\"],\"info\":{\"description\":\"暗精灵王隐藏在幽暗森林中，拥有暗属性力量和技能“暗影之舞”\",\"attack\":85,\"defense\":96,\"grow\":\"{\\\"attack\\\":11,\\\"defense\\\":28}\",\"reward\":\"[{\\\"item\\\":\\\"150k肉\\\",\\\"nums\\\":2},{\\\"item\\\":\\\"150k土\\\",\\\"nums\\\":1},{\\\"item\\\":\\\"蚁国币\\\",\\\"nums\\\":3},{\\\"item\\\":\\\"150k沙\\\",\\\"nums\\\":4},{\\\"nums\\\":3,\\\"item\\\":\\\"1小时加速\\\"}]\",\"chapter\":[\"6-13\",\"2-14\"]},\"equipments\":[{\"synthesis\":[\"冥虹镜芒\"],\"attr\":\"{\\\"attack\\\":15,\\\"defense\\\":10.235}\",\"position\":2,\"type\":\"头盔\",\"is_synthesis\":true}]}]\n\\\"attack\\\":15,\\\"defense\\\":10.235}\",\"position\":2,\"type\":\"头盔\",\"is_synthesis\":true},{\"position\":3,\"type\":\"铠甲\",\"is_synthesis\":true,\"synthesis\":[\"祭祀血袍\",\"女神之袍\",\"守护之铠\"],\"attr\":\"{\\\"defense\\\":10,\\\"attack\\\":3}\"}],\"attack\":3,\"defense\":9,\"is_lock\":true,\"update_time\":\"2024-11-02 19:41:25 \"}\n\\\"attack\\\":5,\\\"defense\\\":5}\"},{\"is_synthesis\":false,\"synthesis\":[],\"attr\":\"{\\\"attack\\\":18,\\\"defense\\\":0}\",\"suit\":\"[{\\\"name\\\":\\\"冥虹镜芒\\\",\\\"suit_attr\\\":{\\\"attack\\\":30,\\\"defense\\\":20.5}},{\\\"name\\\":\\\"镜芒铠\\\",\\\"suit_attr\\\":{\\\"defense\\\":9.99,\\\"attack\\\":10.835}}]\",\"position\":1,\"type\":\"剑\"}],\"bonds\":[\"血红弓背蚁\",\"黑金珠蚁\"],\"grow\":{\"attack\":2,\"defense\":9},\"attack\":9,\"defense\":25,\"update_time\":\"2024-11-02 20:39:30\"}\n\\\"attack\\\":-10,\\\"defense\\\":9}\",\"suit\":\"[{\\\"name\\\":\\\"神明战靴\\\",\\\"suit_attr\\\":{\\\"defense\\\":99.99,\\\"attack\\\":2}},{\\\"name\\\":\\\"碧血手镯\\\",\\\"suit_attr\\\":{\\\"attack\\\":10,\\\"defense\\\":20}}]\",\"position\":4,\"type\":\"袍\"}],\"name\":\"熊猫蚁\"},{\"nickname\":\"合欢树蚁(oxlY)\",\"level\":9,\"is_boss\":false,\"skills\":[\"合欢树蚁003\",\"合欢树蚁0012\",\"合欢树蚁0011\"],\"equipments\":[{\"position\":4,\"type\":\"袍\",\"is_synthesis\":true,\"synthesis\":[\"女神之袍\",\"镜芒铠\",\"守护之铠\"],\"attr\":\"{\\\"attack\\\":-10,\\\"defense\\\":9}\",\"suit\":\"[{\\\"name\\\":\\\"神明战靴\\\",\\\"suit_attr\\\":{\\\"attack\\\":2,\\\"defense\\\":99.99}},{\\\"name\\\":\\\"碧血手镯\\\",\\\"suit_attr\\\":{\\\"defense\\\":20,\\\"attack\\\":10}}]\"},{\"attr\":\"{\\\"attack\\\":2,\\\"defense\\\":15}\",\"position\":3,\"type\":\"铠甲\",\"is_synthesis\":true,\"synthesis\":[\"祭祀血袍\",\"镜芒铠\",\"镜芒铠\"]}],\"name\":\"合欢树蚁\"},{\"info\":{\"defense\":96,\"grow\":\"{\\\"defense\\\":16,\\\"attack\\\":26}\",\"reward\":\"[{\\\"item\\\":\\\"蚁国币\\\",\\\"nums\\\":11},{\\\"nums\\\":1,\\\"item\\\":\\\"150k土\\\"},{\\\"nums\\\":82,\\\"item\\\":\\\"昆虫残骸\\\"},{\\\"item\\\":\\\"8小时加速\\\",\\\"nums\\\":3},{\\\"item\\\":\\\"150k沙\\\",\\\"nums\\\":3}]\",\"chapter\":[\"5-14\",\"6-12\"],\"description\":\"冰霜洞穴的Boss艾尔文以其冰属性攻击闻名，技能“冰封领域”能使区域变得寒冷\",\"attack\":39},\"equipments\":[{\"position\":6,\"type\":\"手镯\",\"is_synthesis\":false,\"synthesis\":[],\"attr\":\"{\\\"attack\\\":5,\\\"defense\\\":5}\"},{\"is_synthesis\":false,\"synthesis\":[],\"attr\":\"{\\\"attack\\\":5,\\\"defense\\\":5}\",\"position\":6,\"type\":\"手镯\"}],\"name\":\"镰刀猛蚁\",\"nickname\":\"镰刀猛蚁(mit2)\",\"level\":7,\"is_boss\":false,\"skills\":[\"镰刀猛蚁0014\",\"镰刀猛蚁001\",\"镰刀猛蚁004\"]}]\\\"attack\\\":28,\\\"defense\\\":25}\",\"reward\":\"[{\\\"item\\\":\\\"昆虫残骸\\\",\\\"nums\\\":66},{\\\"item\\\":\\\"150k土\\\",\\\"nums\\\":1},{\\\"item\\\":\\\"8小时加速\\\",\\\"nums\\\":1},{\\\"item\\\":\\\"150k沙\\\",\\\"nums\\\":4},{\\\"item\\\":\\\"1小时加速\\\",\\\"nums\\\":2}]\",\"chapter\":[\"5-14\",\"5-10\"]},\"equipments\":[{\"attr\":\"{\\\"defense\\\":0,\\\"attack\\\":15}\",\"suit\":\"[{\\\"suit_attr\\\":{\\\"attack\\\":10,\\\"defense\\\":20},\\\"name\\\":\\\"九耀御雷\\\"},{\\\"name\\\":\\\"守护之铠\\\",\\\"suit_attr\\\":{\\\"defense\\\":25,\\\"attack\\\":15}}]\",\"position\":1,\"type\":\"剑\",\"is_synthesis\":false,\"synthesis\":[]},{\"is_synthesis\":true,\"synthesis\":[\"祭祀血袍\",\"镜芒铠\",\"镜芒铠\"],\"attr\":\"{\\\"attack\\\":2,\\\"defense\\\":15}\",\"position\":3,\"type\":\"铠甲\"}],\"name\":\"合欢树蚁\",\"nickname\":\"合欢树蚁(G6Jg)\",\"level\":1},{\"level\":3,\"is_boss\":false,\"skills\":[\"红黑细长蚁004\",\"红黑细长蚁008\",\"红黑细长蚁006\"],\"info\":{\"chapter\":[\"1-1\",\"5-8\"],\"description\":\"奥兹玛是天界之塔的领主，拥有无与伦比的力量和技能“天陨之击”\",\"attack\":64,\"defense\":86,\"grow\":\"{\\\"attack\\\":18,\\\"defense\\\":29}\",\"reward\":\"[{\\\"item\\\":\\\"蚁国币\\\",\\\"nums\\\":4},{\\\"item\\\":\\\"150k沙\\\",\\\"nums\\\":2},{\\\"nums\\\":3,\\\"item\\\":\\\"生物残骸\\\"},{\\\"nums\\\":62,\\\"item\\\":\\\"昆虫残骸\\\"},{\\\"item\\\":\\\"150k土\\\",\\\"nums\\\":2}]\"},\"equipments\":[{\"attr\":\"{\\\"attack\\\":3,\\\"defense\\\":10}\",\"position\":3,\"type\":\"铠甲\",\"is_synthesis\":true,\"synthesis\":[\"祭祀血袍\",\"女神之袍\",\"守护之铠\"]},{\"synthesis\":[\"祭祀血袍\",\"镜芒铠\",\"镜芒铠\"],\"attr\":\"{\\\"attack\\\":2,\\\"defense\\\":15}\",\"position\":3,\"type\":\"铠甲\",\"is_synthesis\":true}],\"name\":\"红黑细长蚁\",\"nickname\":\"红黑细长蚁(TnKh)\"}]\n\\\"defense\\\":29,\\\"attack\\\":25}\",\"reward\":\"[{\\\"item\\\":\\\"150k土\\\",\\\"nums\\\":5},{\\\"item\\\":\\\"150k沙\\\",\\\"nums\\\":4},{\\\"item\\\":\\\"150k肉\\\",\\\"nums\\\":4},{\\\"item\\\":\\\"1小时加速\\\",\\\"nums\\\":5},{\\\"item\\\":\\\"昆虫残骸\\\",\\\"nums\\\":50}]\",\"chapter\":[\"3-1\",\"4-2\"],\"description\":\"奥兹玛是天界之塔的领主，拥有无与伦比的力量和技能“天陨之击”\"},\"equipments\":[{\"attr\":\"{\\\"attack\\\":5,\\\"defense\\\":5}\",\"position\":6,\"type\":\"手镯\",\"is_synthesis\":false,\"synthesis\":[]},{\"type\":\"铠甲\",\"is_synthesis\":true,\"synthesis\":[\"祭祀血袍\",\"镜芒铠\",\"镜芒铠\"],\"attr\":\"{\\\"attack\\\":2,\\\"defense\\\":15}\",\"position\":3},{\"position\":3,\"type\":\"铠甲\",\"is_synthesis\":true,\"synthesis\":[\"祭祀血袍\",\"镜芒铠\",\"镜芒铠\"],\"attr\":\"{\\\"attack\\\":2,\\\"defense\\\":15}\"}]},{\"is_boss\":false,\"skills\":[\"巨型切叶蚁005\",\"巨型切叶蚁001\",\"巨型切叶蚁004\"],\"info\":{\"attack\":59,\"defense\":81,\"grow\":\"{\\\"attack\\\":17,\\\"defense\\\":20}\",\"reward\":\"[{\\\"item\\\":\\\"150k肉\\\",\\\"nums\\\":3},{\\\"item\\\":\\\"150k沙\\\",\\\"nums\\\":1},{\\\"item\\\":\\\"昆虫残骸\\\",\\\"nums\\\":65},{\\\"item\\\":\\\"蚁国币\\\",\\\"nums\\\":1},{\\\"nums\\\":1,\\\"item\\\":\\\"1小时加速\\\"}]\",\"chapter\":[\"3-1\",\"5-3\"],\"description\":\"熔岩洞穴的火龙卡西利亚斯拥有强大的火焰攻击，玩家需要躲避火焰风暴并利用水或风属性攻击对抗\u200C\"},\"equipments\":[{\"attr\":\"{\\\"attack\\\":8,\\\"defense\\\":8}\",\"position\":6,\"type\":\"手镯\",\"is_synthesis\":false,\"synthesis\":[]}],\"name\":\"巨型切叶蚁\",\"nickname\":\"巨型切叶蚁(cSZ0)\",\"level\":4}]\n";
        String condition = "{\"conf_relation\":1,\"indicator_conf_models\":[{\"field_name\":\"enemy.name\",\"sub_field_type\":\"文本\",\"parent_field_type\":\"对象组\",\"field_type\":\"对象组\",\"field_kind\":1,\"interval_type\":16,\"array_filter\":null,\"object_list_filter\":null,\"values\":[\"^.{3}$\"],\"user_grouping_filter\":null,\"dim_attribute\":null,\"virtual_attr\":null}]}\\\\\\\"attack\\\\\\\":-3.875,\\\\\\\"defense\\\\\\\":10}\"],\"user_grouping_filter\":null,\"dim_attribute\":null,\"virtual_attr\":null}]}\\\\\\\"is_synthesis\\\\\\\":true\"],\"user_grouping_filter\":null,\"dim_attribute\":null,\"virtual_attr\":null}]}";
        Boolean evaluate = udf.evaluate(s, "enemy", condition, 3);
        System.out.printf(evaluate.toString());
    }
}