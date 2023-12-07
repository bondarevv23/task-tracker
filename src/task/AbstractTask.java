package task;

import manager.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class AbstractTask {
    protected Long id;

    protected String name;

    protected String description;

    protected Status status;

    protected TaskType taskType;

    protected AbstractTask(String name, String description, TaskType taskType) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        this.taskType = taskType;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TaskType getType() {
        return taskType;
    }

    abstract LocalDateTime getStartTime();

    abstract Duration getDuration();

    abstract LocalDateTime getEndTime();
}
