package com.bing.tpa.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtils {
    public static Timestamp StringToTimestamp(String time){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = null;
        try {
            Date date = dateFormat.parse(time);
            timestamp = new Timestamp(date.getTime());
            System.out.println(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public  static String TimeToString(LocalDateTime time){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        return time.format(formatter);
    }

    public static String getTimeDeff(LocalDateTime beginTime, LocalDateTime now) {
        //       2.1获取时间差，设置题目完成总耗时
        Duration duration = Duration.between(beginTime, now);
        // 获取总秒数
        long totalSeconds = duration.getSeconds();
        // 计算小时、分钟和秒
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        // 格式化输出
        String timeDifference = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeDifference;
    }

    public static LocalDateTime stringToLocalDateTime(String time){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime deadline = LocalDateTime.parse(time,formatter);
        return deadline;
    }
}
