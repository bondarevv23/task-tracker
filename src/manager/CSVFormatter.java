package manager;

import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVFormatter {
    public static final String delimiter = ",";

    public static String toString(Task task) {
        return Stream.of(
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                task.getStartTime(),
                task.getDuration()
        ).map(Objects::toString).collect(Collectors.joining(delimiter));
    }

    public static Task parseTask(String value) {
        String[] splitValue = value.split(delimiter);
        if (TaskType.valueOf(splitValue[1]) != TaskType.TASK) {
            throw new IllegalArgumentException("the type of passed string is not Task");
        }
        Task task = new Task(
                splitValue[2],
                splitValue[4],
                LocalDateTime.parse(splitValue[5]),
                Duration.parse(splitValue[6])
        );
        task.setId(Long.parseLong(splitValue[0]));
        task.setStatus(Status.valueOf(splitValue[3]));
        return task;
    }

    public static String toString(Subtask subtask) {
        return Stream.of(
                subtask.getId(),
                subtask.getType(),
                subtask.getName(),
                subtask.getStatus(),
                subtask.getDescription(),
                subtask.getParentEpic().getId(),
                subtask.getStartTime(),
                subtask.getDuration()
        ).map(Objects::toString).collect(Collectors.joining(delimiter));
    }

    public static Subtask parseSubtask(String value, Function<Long, Epic> getEpicById) {
        String[] splitValue = value.split(delimiter);
        if (TaskType.valueOf(splitValue[1]) != TaskType.SUBTASK) {
            throw new IllegalArgumentException("the type of passed string is not Subtask");
        }
        Subtask subtask = new Subtask(
                splitValue[2],
                splitValue[4],
                getEpicById.apply(Long.parseLong(splitValue[5])),
                LocalDateTime.parse(splitValue[6]),
                Duration.parse(splitValue[7])
        );
        subtask.setId(Long.parseLong(splitValue[0]));
        subtask.setStatus(Status.valueOf(splitValue[3]));
        return subtask;
    }

    public static String toString(Epic epic) {
        return Stream.of(
                epic.getId(),
                epic.getType(),
                epic.getName(),
                epic.getStatus(),
                epic.getDescription()
        ).map(Objects::toString).collect(Collectors.joining(delimiter));
    }

    public static Epic parseEpic(String value) {
        String[] splitValue = value.split(delimiter);
        if (TaskType.valueOf(splitValue[1]) != TaskType.EPIC) {
            throw new IllegalArgumentException("the type of passed string is not Epic");
        }
        Epic epic = new Epic(splitValue[2], splitValue[4]);
        epic.setId(Long.parseLong(splitValue[0]));
        epic.setStatus(Status.valueOf(splitValue[3]));
        return epic;
    }

    public static String toString(HistoryManager historyManager) {
        return historyManager.getHistory().stream()
                .map(AbstractTask::getId)
                .map(Objects::toString)
                .collect(Collectors.joining(delimiter));
    }

    static List<Long> parseHistory(String value) {
        return Arrays.stream(value.split(delimiter)).map(Long::parseLong).collect(Collectors.toList());
    }
}
