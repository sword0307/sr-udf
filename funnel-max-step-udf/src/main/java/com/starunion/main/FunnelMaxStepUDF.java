package com.starunion.main;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FunnelMaxStepUDF {
    public Integer evaluate(String param, Integer conditionStep) {
        Integer initStep = 0;
        try {
            String[] arrays = param.split(",");
            List<Integer> paramList = Arrays.stream(arrays).map(Integer::new).collect(Collectors.toList());
            for (int i = 0; i < paramList.size(); i++) {
                Integer nextParam = paramList.get(i);
                if ((nextParam & (1 << initStep)) == (1 << initStep)) {
                    initStep += 1;
                    if (initStep == conditionStep) {
                        return initStep;
                    }
                }
            }
        } catch (Exception e) {
            return 0;
        }
        return initStep;
    }
}
