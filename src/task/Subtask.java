package task;

import lombok.Builder;
import manager.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends AbstractTimeTask {

    private Epic parentEpic;

    @Builder
    public Subtask(String name, String description, Epic epic, LocalDateTime startTime, Duration duration) {
        super(name, description, TaskType.SUBTASK, startTime, duration);
        this.parentEpic = epic;
        parentEpic.addSubtask(this);
    }

    public Epic getParentEpic() {
        return parentEpic;
    }

    public void setParentEpic(Epic parentEpic) {
        this.parentEpic = parentEpic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subtask subtask = (Subtask) o;
        return Objects.equals(getId(), subtask.getId()) &&
                Objects.equals(getName(), subtask.getName()) &&
                Objects.equals(getDescription(), subtask.getDescription()) &&
                Objects.equals(getStatus(), subtask.getStatus()) &&
                Objects.equals(getParentEpic(), subtask.getParentEpic());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getStatus(), getParentEpic());
    }

    @Override
    public String toString() {
        return "Subtask{" +
                " id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", parentEpicId=" + getParentEpic().getId() +
                '}';
    }
}
