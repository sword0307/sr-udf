package com.starunion.main;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.google.gson.JsonSyntaxException;


public class JsonArrayToArrayUDF {
    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<ArrayList<String>>() {
    }.getType();


    public ArrayList<String> evaluate(String input) {
        ArrayList<String> list = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return list;
        }

        try {
            // Use Gson to parse the JSON array string
            return gson.fromJson(input, listType);
        } catch (JsonSyntaxException e) {
            // Handle parsing error
            return list;
        }
    }


//    public static void main(String[] args) {
//        JsonArrayToArrayUDF jsonArrayToArrayUDF = new JsonArrayToArrayUDF();
//        String s ="[]";
//        ArrayList<String> resoult = jsonArrayToArrayUDF.evaluate(s);
//        System.out.println(resoult.toString());
//    }
}
