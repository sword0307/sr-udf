package com.starunion.main;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import com.google.gson.JsonSyntaxException;


public class ArrayToJsonArrayUDF {
    private static final Gson gson = new Gson();
    public String evaluate(ArrayList<String> input) {
        if (input == null){
            return "";
        }
        return gson.toJson(input);
    }


//    public static void main(String[] args) {
//        ArrayToJsonArrayUDF jsonArrayToArrayUDF = new ArrayToJsonArrayUDF();
//        ArrayList<String> arrayList = new ArrayList<>();
//        arrayList.add("1444444");
//        arrayList.add("2");
//         String  resoult= jsonArrayToArrayUDF.evaluate(arrayList);
//        System.out.println(resoult);
//    }
}
