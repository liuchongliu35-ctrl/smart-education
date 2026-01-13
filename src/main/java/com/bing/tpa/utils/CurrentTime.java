package com.bing.tpa.utils;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 该类用于获取当前时间
 */
@Component
public class CurrentTime {
    public static LocalDateTime getTime() {
        LocalDateTime localtime = null;
        try {
            localtime = LocalDateTime.now();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localtime;
    }
}
