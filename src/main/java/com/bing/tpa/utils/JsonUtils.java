package com.bing.tpa.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.*;

public class JsonUtils {
    public static String sortAndFormatJson(String inputJson) throws IOException {
        // 创建ObjectMapper实例
        ObjectMapper mapper = new ObjectMapper();

        // 禁用美化输出（确保紧凑格式）
        mapper.disable(SerializationFeature.INDENT_OUTPUT);

        // 将JSON解析为Map<String, Double>
        Map<String, Double> originalMap = mapper.readValue(inputJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Double>>() {});

        // 使用TreeMap按键的数值排序（自定义比较器）
        Map<Integer, Double> sortedMap = new TreeMap<>();
        for (Map.Entry<String, Double> entry : originalMap.entrySet()) {
            sortedMap.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }

        // 创建有序的LinkedHashMap（保持排序后的顺序）
        Map<String, Double> orderedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
            orderedMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        // 生成紧凑格式的JSON字符串
        String outputJson = mapper.writeValueAsString(orderedMap);
        System.out.println(outputJson);
        return outputJson;
    }



}
