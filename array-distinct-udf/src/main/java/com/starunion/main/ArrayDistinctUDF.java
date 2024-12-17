package com.starunion.main;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.JsonSyntaxException;


public class ArrayDistinctUDF {
    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<ArrayList<String>>() {
    }.getType();


    public String evaluate(String input) {
        ArrayList<String> list;
        if (input == null) {
            return null;
        }
        if (input.isEmpty()) {
            return "";
        }
        try {
            list = gson.fromJson(input, listType);
            List<String> uniqueSortedList = list.stream()
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList());
            return gson.toJson(uniqueSortedList);
        } catch (JsonSyntaxException e) {
            return "";
        }
    }


//    public static void main(String[] args) {
//        ArrayDistinctUDF jsonArrayToArrayUDF = new ArrayDistinctUDF();
//        String s = "['1ssss','1','3ss','4ddd','3','4']";
//        String resoult = jsonArrayToArrayUDF.evaluate(s);
//        System.out.println(resoult);
//    }
}
