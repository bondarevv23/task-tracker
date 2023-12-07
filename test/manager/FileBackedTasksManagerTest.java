package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import task.AbstractTask;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTasksManagerTest extends AbstractTaskManagerTest {
    private final Path testPath = Path.of("tmp");

    @Override
    TaskManager taskManager() {
        return new FileBackedTasksManager(testPath);
    }

    @AfterEach
    public void deleteFile() {
        try {
            Files.deleteIfExists(testPath);
        } catch (IOException | SecurityException exception) {
            // ignored
        }
    }

    private void assertTaskInReader(List<Task> list, BufferedReader reader) {
        list.forEach(x -> {
            try {
                assertEquals(CSVFormatter.toString(x), reader.readLine());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private void assertSubtaskInReader(List<Subtask> list, BufferedReader reader) {
        list.forEach(x -> {
            try {
                assertEquals(CSVFormatter.toString(x), reader.readLine());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private void assertEpicInReader(List<Epic> list, BufferedReader reader) {
        list.forEach(x -> {
            try {
                assertEquals(CSVFormatter.toString(x), reader.readLine());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private void assertHistory(BufferedReader reader) {
        try {
            assertTrue(reader.readLine().isEmpty());
            assertEquals(
                    taskManager.getHistory().stream()
                            .map(AbstractTask::getId)
                            .map(Objects::toString)
                            .collect(Collectors.joining(",")),
                    reader.readLine()
            );
        } catch (IOException exception) {
            throw new RuntimeException();
        }
    }

    @Test
    void save() {
        List<Task> tasks = getTasks(3);
        List<Epic> epics = getEpics(2);
        List<Subtask> epic1subtasks = getSubtasks(3, epics.get(0));
        List<Subtask> epic2subtasks = getSubtasks(2, epics.get(1));
        tasks.forEach(task -> taskManager.getTaskById(task.getId()));

        try (BufferedReader reader = Files.newBufferedReader(testPath)) {
            assertTaskInReader(tasks, reader);
            assertEpicInReader(epics, reader);
            assertSubtaskInReader(epic1subtasks, reader);
            assertSubtaskInReader(epic2subtasks, reader);
            assertHistory(reader);
        } catch (IOException | SecurityException | IllegalArgumentException exception) {
            throw new RuntimeException();
        }
    }

    @Test
    void save_withoutTasks() {
        List<Epic> epics = getEpics(2);
        List<Subtask> epic1subtasks = getSubtasks(3, epics.get(0));
        List<Subtask> epic2subtasks = getSubtasks(2, epics.get(1));
        taskManager.getEpicById(epics.get(0).getId());
        taskManager.getSubtaskById(epic2subtasks.get(0).getId());

        try (BufferedReader reader = Files.newBufferedReader(testPath)) {
            assertEpicInReader(epics, reader);
            assertSubtaskInReader(epic1subtasks, reader);
            assertSubtaskInReader(epic2subtasks, reader);
            assertHistory(reader);
        } catch (IOException | SecurityException | IllegalArgumentException exception) {
            throw new RuntimeException();
        }
    }

    @Test
    void save_empty() {
        assertFalse(Files.exists(testPath));
    }

    @Test
    void save_EpicWithoutSubtasks() {
        List<Epic> epics = getEpics(2);
        epics.forEach(epic -> taskManager.getEpicById(epic.getId()));

        try (BufferedReader reader = Files.newBufferedReader(testPath)) {
            assertEpicInReader(epics, reader);
            assertHistory(reader);
        } catch (IOException | SecurityException | IllegalArgumentException exception) {
            throw new RuntimeException();
        }
    }

    @Test
    void save_emptyHistory() {
        List<Task> tasks = getTasks(3);
        List<Epic> epics = getEpics(2);
        List<Subtask> epic1subtasks = getSubtasks(3, epics.get(0));
        List<Subtask> epic2subtasks = getSubtasks(2, epics.get(1));

        try (BufferedReader reader = Files.newBufferedReader(testPath)) {
            assertTaskInReader(tasks, reader);
            assertEpicInReader(epics, reader);
            assertSubtaskInReader(epic1subtasks, reader);
            assertSubtaskInReader(epic2subtasks, reader);
            assertNull(reader.readLine());
        } catch (IOException | SecurityException | IllegalArgumentException exception) {
            throw new RuntimeException();
        }
    }

    private void writeToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(testPath)) {
            for (Task task : taskManager.getTasksList()) {
                writer.write(CSVFormatter.toString(task));
                writer.newLine();
            }
            for (Epic epic :taskManager.getEpicsList()) {
                writer.write(CSVFormatter.toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : taskManager.getSubtasksList()) {
                writer.write(CSVFormatter.toString(subtask));
                writer.newLine();
            }
            if (taskManager.getHistory().isEmpty()) {
                return;
            }
            writer.newLine();
            writer.write(taskManager.getHistory().stream()
                    .map(AbstractTask::getId)
                    .map(Objects::toString)
                    .collect(Collectors.joining(",")));
        } catch (IOException | SecurityException exception) {
            throw new RuntimeException();
        }
    }

    private void assertTaskManagersEquals(TaskManager taskManager1, TaskManager taskManager2) {
        assertEquals(taskManager1.getTasksList(), taskManager2.getTasksList());
        assertEquals(taskManager1.getEpicsList(), taskManager2.getEpicsList());
        assertEquals(taskManager1.getSubtasksList(), taskManager2.getSubtasksList());
        assertEquals(taskManager1.getHistory(), taskManager2.getHistory());
    }

    @Test
    void loadFromFile() {
        List<Task> tasks = getTasks(3);
        List<Epic> epics = getEpics(2);
        List<Subtask> epic1subtasks = getSubtasks(3, epics.get(0));
        List<Subtask> epic2subtasks = getSubtasks(2, epics.get(1));
        tasks.forEach(task -> taskManager.getTaskById(task.getId()));

        writeToFile();

        FileBackedTasksManager tasksManager2 = FileBackedTasksManager.loadFromFile(testPath);

        assertTaskManagersEquals(taskManager, tasksManager2);
    }

    @Test
    void loadFromFile_empty() {
        try (BufferedWriter writer = Files.newBufferedWriter(testPath)) {
            // do nothing
        } catch (IOException | SecurityException exception) {
            throw new RuntimeException();
        }

        FileBackedTasksManager tasksManager2 = FileBackedTasksManager.loadFromFile(testPath);

        assertTaskManagersEquals(taskManager, tasksManager2);
    }

    @Test
    void loadFromFile_epicWithoutSubtasks() {
        List<Epic> epics = getEpics(5);
        epics.forEach(epic -> taskManager.getEpicById(epic.getId()));

        writeToFile();

        FileBackedTasksManager tasksManager2 = FileBackedTasksManager.loadFromFile(testPath);

        assertTaskManagersEquals(taskManager, tasksManager2);
    }

    @Test
    void loadFromFile_emptyHistory() {
        List<Task> tasks = getTasks(10);
        List<Epic> epics = getEpics(50);
        List<Subtask> subtasks1 = getSubtasks(10, epics.get(5));
        List<Subtask> subtasks2 = getSubtasks(10, epics.get(10));
        List<Subtask> subtasks3 = getSubtasks(10, epics.get(15));
        List<Subtask> subtasks4 = getSubtasks(10, epics.get(20));
        List<Subtask> subtasks5 = getSubtasks(10, epics.get(25));

        writeToFile();

        FileBackedTasksManager tasksManager2 = FileBackedTasksManager.loadFromFile(testPath);

        assertTaskManagersEquals(taskManager, tasksManager2);
    }
}
