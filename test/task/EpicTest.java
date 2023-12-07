package task;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private final LocalDateTime time = LocalDateTime.of(1, 1, 1, 0, 0, 0);
    private final Duration duration = Duration.ZERO;
    private TaskManager taskManager;

    private Task newTask(String name, String desc) {
        return new Task(name, desc, time, duration);
    }

    private Epic newEpic(String name, String desc) {
        return new Epic(name, desc);
    }

    private Subtask newSubtask(String name, String desc, Epic epic) {
        return new Subtask(name, desc, epic, time, duration);
    }

    @BeforeEach
    public void init() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void emptySubtasksList() {
        Epic epic = taskManager.addNewEpic(newEpic("name1", "desc1"));

        assertTrue(epic.getSubtasks().isEmpty());
        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void subtasksNew() {
        Epic epic = taskManager.addNewEpic(newEpic("name1", "desc1"));
        taskManager.addNewSubtask(newSubtask("name1", "desc1", epic));
        taskManager.addNewSubtask(newSubtask("name2", "desc2", epic));
        taskManager.addNewSubtask(newSubtask("name3", "desc3", epic));

        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void subtasksAreDone() {
        Epic epic = taskManager.addNewEpic(newEpic("name1", "desc1"));
        List<Subtask> subtasks = List.of(
                newSubtask("name1", "desc1", epic),
                newSubtask("name2", "desc2", epic),
                newSubtask("name3", "desc3", epic)
        );
        subtasks.forEach(taskManager::addNewSubtask);

        subtasks.forEach(subtask -> subtask.setStatus(Status.DONE));
        subtasks.forEach(taskManager::updateSubtask);

        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void subtasksWithNewAndDone() {
        Epic epic = taskManager.addNewEpic(newEpic("name1", "desc1"));
        List<Subtask> subtasks = List.of(
                newSubtask("name1", "desc1", epic),
                newSubtask("name2", "desc2", epic),
                newSubtask("name3", "desc3", epic)
        );

        subtasks.forEach(taskManager::addNewSubtask);

        subtasks.subList(0, 2).forEach(subtask -> subtask.setStatus(Status.DONE));
        subtasks.forEach(taskManager::updateSubtask);

        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void subtasksInProgress() {
        Epic epic = taskManager.addNewEpic(newEpic("name1", "desc1"));
        List<Subtask> subtasks = List.of(
                newSubtask("name1", "desc1", epic),
                newSubtask("name2", "desc2", epic),
                newSubtask("name3", "desc3", epic)
        );
        subtasks.forEach(taskManager::addNewSubtask);

        subtasks.forEach(subtask -> subtask.setStatus(Status.IN_PROGRESS));
        subtasks.forEach(taskManager::updateSubtask);

        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }
}
