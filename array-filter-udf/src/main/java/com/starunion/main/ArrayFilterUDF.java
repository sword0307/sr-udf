package com.starunion.main;

import java.util.*;

public class ArrayFilterUDF {

    public Boolean evaluate(ArrayList<String> list1, ArrayList<String> list2, Integer judgeType) {
        if ((list2 == null) || (list2.isEmpty()) || (judgeType == 0)) {
            return false;
        }


        // 全部存在
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
            // 全部不存在
            if ((list1 == null) ) {
                return false;
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

//    public static void main(String[] args) {
//        ArrayList<String> list1 = null;
//
//        ArrayList<String> list2 = new ArrayList<>();
//        list2.add("5");
//        list2.add("4");
//
//
//        ArrayFilterUDF arrayFilterUDF = new ArrayFilterUDF();
//        Boolean result = arrayFilterUDF.evaluate(list1, list2, 2);
//        System.out.println(result);
//    }
}