package com.bing.tpa.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryDataStore {
    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        dataMap.put(key, value);
    }

    public Object get(String key) {
        return dataMap.get(key);
    }

    public void remove(String key) {
        dataMap.remove(key);
    }

    public boolean containsKey(String key) {
        return dataMap.containsKey(key);
    }
}
