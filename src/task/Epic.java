package task;

import manager.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Epic extends AbstractTask {

    private List<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description, TaskType.EPIC);
        this.subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public void clear() {
        setStatus(Status.NEW);
        subtasks.clear();
    }

    public Epic addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        return this;
    }

    private List<Long> getSubtaskIds() {
        return getSubtasks().stream().map(Subtask::getId).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Epic{" +
                " id=" + getId() +
                ",name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                '}';
    }

    @Override
    public LocalDateTime getStartTime() {
        return subtasks.stream().map(Subtask::getStartTime).min(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public Duration getDuration() {
        return subtasks.stream().map(Subtask::getDuration).reduce(Duration::plus).orElse(Duration.ZERO);
    }

    @Override
    public LocalDateTime getEndTime() {
        return subtasks.stream().map(Subtask::getEndTime).max(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return Objects.equals(getSubtaskIds(), epic.getSubtaskIds()) && Objects.equals(getId(), epic.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubtaskIds(), getId());
    }
}
