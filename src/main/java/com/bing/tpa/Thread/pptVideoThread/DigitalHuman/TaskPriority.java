package com.bing.tpa.Thread.pptVideoThread.DigitalHuman;

// 任务优先级枚举
public enum TaskPriority {
    HIGH(2), NORMAL(1), LOW(0);

    public final int value;

    TaskPriority(int value) {
        this.value = value;
    }
}
