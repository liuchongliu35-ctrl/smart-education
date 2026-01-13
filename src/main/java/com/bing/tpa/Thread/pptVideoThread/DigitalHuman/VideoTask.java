package com.bing.tpa.Thread.pptVideoThread.DigitalHuman;

import lombok.Getter;
import lombok.Setter;

// 视频任务抽象类
@Getter
public abstract class VideoTask implements Runnable {
    @Setter
    private TaskPriority priority = TaskPriority.NORMAL;
    private String taskId;

    public VideoTask(String taskId) {
        this.taskId = taskId;
    }
    @Override
    public void run() {
        try {
            execute();
        } catch (Exception e) {
            onError(e);
        }
    }
    public abstract void execute() throws Exception;

    public void onInterrupted() {
        System.out.println("Task interrupted: " + taskId);
    }

    public void onError(Exception e) {
        System.err.println("Task failed: " + taskId + " - " + e.getMessage());
    }

    public void onRejected() {
        System.out.println("Task rejected: " + taskId);
    }

}
