package com.starunion.main;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JsonArrayFilterUDF {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private boolean TextCheck(String str, Integer intervalType, ArrayList<String> values) {
        switch (intervalType) {
            case 1:
                //等于
                return isTextInArray(values, str);
            case 10:
                //不等于
                return !isTextInArray(values, str);
            case 11:
                //包含
                return str.contains(values.get(0));
            case 12:
                //不包含
                return !str.contains(values.get(0));
            case 13:
                //为空
                return str == null;
            case 14:
                //非空
                return !(str == null);
            case 15:
                //正则匹配
                return Pattern.compile(values.get(0)).matcher(str).find();
            case 16:
                //正则不匹配
                return !Pattern.compile(values.get(0)).matcher(str).find();
            default:
                return false;
        }
    }


    private boolean NumberCheck(Double num, Integer intervalType, ArrayList<String> values) {
        switch (intervalType) {
            case 1:
                //等于
                return isNumberInArray(convertStringArrayToDoubleArray(values), num);
            case 10:
                //不等于
                return !isNumberInArray(convertStringArrayToDoubleArray(values), num);
            case 2:
                //大于
                return num > Double.parseDouble(values.get(0));
            case 4:
                //小于
                return num < Double.parseDouble(values.get(0));
            case 3:
                //大于等于
                return num >= Double.parseDouble(values.get(0));
            case 5:
                //小于等于
                return num <= Double.parseDouble(values.get(0));
            case 6:
                //闭区间
                return num >= Double.parseDouble(values.get(0)) && num <= Double.parseDouble(values.get(1));
            case 9:
                //开区间
                return num > Double.parseDouble(values.get(0)) && num < Double.parseDouble(values.get(1));
            case 7:
                //左开右闭
                return num > Double.parseDouble(values.get(0)) && num <= Double.parseDouble(values.get(1));
            case 8:
                //左闭右开
                return num >= Double.parseDouble(values.get(0)) && num < Double.parseDouble(values.get(1));
            case 13:
                //为空
                return num == null;
            case 14:
                //非空
                return !(num == null);
            default:
                return false;
        }

    }


    private boolean BooleanCheck(Boolean b, Integer intervalType, ArrayList<String> values) {
        switch (intervalType) {
            case 13:
                //为空
                return b == null;
            case 14:
                //非空
                return !(b == null);
            case 19:
                //为真
                return b;
            case 20:
                //为假
                return !b;
            default:
                return false;
        }
    }


    private boolean ListCheck(List<String> list1, ArrayList<String> list2, Integer judgeType) {
        if ((list2 == null) || (list2.isEmpty()) || (judgeType == 0)) {
            return false;
        }

        // 存在
        if (judgeType == 1) {
            if ((list1 == null) || (list1.isEmpty())) {
                return false;
            }
            for (String str1 : list1) {
                for (String str2 : list2) {
                    if (str1.equals(str2)) {
                        return true;
                    }
                }
            }
            return false;
        } else if (judgeType == 2) {
            // 不存在
            if ((list1 == null) || (list1.isEmpty())) {
                return true;
            }
            boolean allNotInList2 = true;
            for (String str1 : list1) {
                for (String str2 : list2) {
                    if (str1.equals(str2)) {
                        allNotInList2 = false;
                        break;
                    }
                }
                if (!allNotInList2) {
                    break;
                }
            }
            return allNotInList2;
        }
        return false;
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

    public Boolean evaluate(String jsonStr, String jsonPath, String subFieldType, Integer intervalType, Integer elementJudge, ArrayList<String> values) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            if (intervalType == 13) {
                return true;
            }
            return false;
        }
        try {
            Object obj = JsonPath.read(jsonStr, jsonPath);
            if (obj == null && intervalType == 13) {
                return true;
            }
            switch (subFieldType) {
                case "文本":
                    if (obj instanceof List<?>) {
                        List<String> dest = objectMapper.convertValue(obj, new TypeReference<List<String>>() {
                        });
                        for (String text : dest) {
                            if (TextCheck(text, intervalType, values)) {
                                return true;
                            }
                        }
                    } else {
                        String dest = objectMapper.convertValue(obj, new TypeReference<String>() {
                        });
                        if (TextCheck(dest, intervalType, values)) {
                            return true;
                        }
                    }
                    return false;
                case "数值":
                    if (obj instanceof List<?>) {
                        List<Double> dest = objectMapper.convertValue(obj, new TypeReference<List<Double>>() {
                        });
                        for (Double num : dest) {
                            if (NumberCheck(num, intervalType, values)) {
                                return true;
                            }
                        }
                    } else {
                        Double dest = objectMapper.convertValue(obj, new TypeReference<Double>() {
                        });
                        if (NumberCheck(dest, intervalType, values)) {
                            return true;
                        }
                    }
                    return false;
                case "布尔":
                    if (obj instanceof List<?>) {
                        List<Boolean> dest = objectMapper.convertValue(obj, new TypeReference<List<Boolean>>() {
                        });
                        for (Boolean b : dest) {
                            if (BooleanCheck(b, intervalType, values)) {
                                return true;
                            }
                        }
                    } else {
                        Boolean dest = objectMapper.convertValue(obj, new TypeReference<Boolean>() {
                        });
                        if (BooleanCheck(dest, intervalType, values)) {
                            return true;
                        }
                    }
                    return false;
                case "列表":
//                    Boolean f;
//                    if (jsonPath.matches("^\\$(\\[\\d+\\])?$")) {
//                        f = true;
//                    }
                    switch (elementJudge) {
                        case 1:
                            List<String> listStringResult = null;
                            List<List<String>> listOfListStringResult = null;
                            if (obj instanceof List<?>) {
                                List<?> resultList = (List<?>) obj;
                                // 检查第一个元素的类型以确定是否为嵌套列表
                                if (!resultList.isEmpty() && resultList.get(0) instanceof List<?>) {
                                    // 将结果转换为 List<List<String>>
                                    listOfListStringResult = objectMapper.convertValue(resultList, new TypeReference<List<List<String>>>() {
                                    });
                                    for (List<String> l1 : listOfListStringResult) {
                                        if (ListCheck(l1, values, elementJudge)) {
                                            return true;
                                        }
                                    }
                                } else {
                                    // 将结果转换为 List<String>
                                    listStringResult = objectMapper.convertValue(resultList, new TypeReference<List<String>>() {
                                    });
                                    if (ListCheck(listStringResult, values, elementJudge)) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        case 2:
                            List<String> listStringResult2 = null;
                            List<List<String>> listOfListStringResult2 = null;
                            if (obj instanceof List<?>) {
                                List<?> resultList = (List<?>) obj;
                                // 检查第一个元素的类型以确定是否为嵌套列表
                                if (!resultList.isEmpty() && resultList.get(0) instanceof List<?>) {
                                    // 将结果转换为 List<List<String>>
                                    listOfListStringResult2 = objectMapper.convertValue(resultList, new TypeReference<List<List<String>>>() {
                                    });
                                    for (List<String> l1 : listOfListStringResult2) {
                                        if (ListCheck(l1, values, elementJudge)) {
                                            return true;
                                        }
                                    }
                                } else {
                                    // 将结果转换为 List<String>
                                    listStringResult2 = objectMapper.convertValue(resultList, new TypeReference<List<String>>() {
                                    });
                                    if (ListCheck(listStringResult2, values, elementJudge)) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        case 3:
                            if (obj instanceof List<?>) {
                                List<String> stringList = objectMapper.convertValue(obj, new TypeReference<List<String>>() {
                                });
                                for (String s : stringList) {
                                    if (TextCheck(s, intervalType, values)) {
                                        return true;
                                    }
                                }
                            } else {
                                String dest = objectMapper.convertValue(obj, new TypeReference<String>() {
                                });
                                if (TextCheck(dest, intervalType, values)) {
                                    return true;
                                }
                            }
                            return false;
                    }
                default:
//                    Boolean f = null;
//                    int size = 0;
//                    int satisfied = 0;
//                    if (jsonPath.matches("^\\$(\\[\\d+\\])?$")) {
//                        f = true;
//                    }
                    if (intervalType == 13) {
                        if (obj instanceof List<?>) {
                            List<?> resultList = (List<?>) obj;
                            for (Object re : resultList) {
                                if (re == null) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            return obj == null;
                        }
                    } else {
                        if (obj instanceof List<?>) {
                            List<?> resultList = (List<?>) obj;
                            for (Object re : resultList) {
                                if (re != null) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            return obj != null;
                        }
                    }
            }
        } catch (PathNotFoundException e) {
            if (intervalType == 13) {
                return true;
            }
            return false; // 如果没有任何匹配项，返回 false
        }
    }

    public static void main(String[] args) {
        String json = "{\"grow\":{\"attack\":8,\"defense\":6},\"info\":{\"uuid\":\"48754a01-0bc9-4bcc-81c2-33b98ba14118\",\"is_awaken\":false,\"awaken_level\":0,\"awaken_attr\":\"{}\",\"awaken_skills\":[]},\"equipments\":[{\"synthesis\":[\"冥虹镜芒\"],\"attr\":\"{\\\"attack\\\":15,\\\"defense\\\":10.235}\",\"position\":2,\"type\":\"头盔\",\"is_synthesis\":true},{\"position\":3,\"type\":\"铠甲\",\"is_synthesis\":true,\"synthesis\":[\"祭祀血袍\",\"女神之袍\",\"守护之铠\"],\"attr\":\"{\\\"defense\\\":10,\\\"attack\\\":3}\"}],\"attack\":3,\"defense\":9,\"is_lock\":true,\"update_time\":\"2024-11-02 19:41:25 \"}\n";
        JsonArrayFilterUDF jsonArrayFilterUDF = new JsonArrayFilterUDF();
        ArrayList<String> list2 = new ArrayList<>();
        list2.add("红黑细长蚁001");
        Boolean b = jsonArrayFilterUDF.evaluate("", "$", "列表", 13, 1, list2);
        System.out.println(b);
    }
}