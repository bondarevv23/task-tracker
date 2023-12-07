package task;

import manager.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class AbstractTimeTask extends AbstractTask {
    private LocalDateTime startTime;
    private Duration duration;

    protected AbstractTimeTask(String name, String description, TaskType taskType, LocalDateTime startTime, Duration duration) {
        super(name, description, taskType);
        this.startTime = startTime;
        this.duration = duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }
}
