package com.fis.epm.utils;

import java.util.List;
import java.util.Map;

public class ValidationUtils {
    public static boolean isNullOrEmpty(String st) {
        return st == null || st.isEmpty();
    }

    public static boolean isNullOrEmpty(Object obj) {
        return obj == null || obj.toString().isEmpty();
    }

    public static boolean isNullOrEmpty(List<?> lst) {
        return lst == null || lst.isEmpty();
    }

    public static boolean isNullOrEmpty(Map map) {
        return map == null || map.isEmpty();
    }
}
