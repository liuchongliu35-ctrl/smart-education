package com.bing.tpa.utils;

import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

public class CronExpressionUtil {

    /**
     * 获取Cron表达式的下一个执行时间
     * @param cronExpression Cron表达式
     * @param currentTime 当前时间
     * @return 下一个执行时间
     */
    public static Date getNextExecutionTime(String cronExpression, Date currentTime) {
        CronSequenceGenerator generator = new CronSequenceGenerator(cronExpression);
        return generator.next(currentTime);
    }
}
