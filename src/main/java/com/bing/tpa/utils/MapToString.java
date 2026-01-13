package com.bing.tpa.utils;

import java.util.Map;
import java.util.stream.Collectors;

public class MapToString {

    public static Map<String, String> convertValuesToString(Map<String, Object> originalMap) {
        return originalMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() != null ? entry.getValue().toString() : ""
                ));
    }
}
