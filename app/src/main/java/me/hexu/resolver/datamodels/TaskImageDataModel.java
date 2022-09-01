package me.hexu.resolver.datamodels;

import java.io.Serializable;

public class TaskImageDataModel implements Serializable {
    private final String taskImageUrl;    // URL изображения из задачи

    public TaskImageDataModel(String taskImageUrl) {
        this.taskImageUrl = taskImageUrl;
    }

    public String getTaskImageUrl() {
        return taskImageUrl;
    }
}
