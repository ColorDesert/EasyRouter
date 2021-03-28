package com.desert.router.mapping;

import java.util.HashMap;
import java.util.Map;

public class RouterMapping123 {

    public static Map<String, String> get() {
        Map<String, String> map = new HashMap<>();
        map.put("router://xxxA", "com.xxx.xxx.AActivity");
        map.put("router://xxxB", "com.xxx.xxx.BActivity");
        map.put("router://xxxC", "com.xxx.xxx.CActivity");
        map.put("router://xxxD", "com.xxx.xxx.DActivity");
        return map;
    }
}
