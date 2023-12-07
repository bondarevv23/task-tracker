package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

abstract class AbstractTaskManagerTest {
    private static final LocalDateTime TIME = LocalDateTime.of(1, 1, 1, 0, 0, 0);
    private static final Duration DURATION = Duration.ZERO.plusMinutes(5);
    protected TaskManager taskManager;
    private IdGenerator startTimeGenerator;
    abstract TaskManager taskManager();

    protected Task newTask(String name, String desc) {
        return taskManager.addNewTask(
                new Task(name, desc, TIME.plusHours(startTimeGenerator.generateId()), DURATION)
        );
    }

    protected Epic newEpic(String name, String desc) {
        return taskManager.addNewEpic(new Epic(name, desc));
    }

    protected Subtask newSubtask(String name, String desc, Epic epic) {
        return taskManager.addNewSubtask(
                new Subtask(name, desc, epic, TIME.plusHours(startTimeGenerator.generateId()), DURATION)
        );
    }

    protected List<Task> getTasks(int count) {
        return IntStream.range(1, 1 + count).mapToObj(i -> newTask("name" + i, "desc" + i))
                .collect(Collectors.toList());
    }

    protected List<Epic> getEpics(int count) {
        return IntStream.range(1, 1 + count).mapToObj(i -> newEpic("name" + i, "desc" + i))
                .collect(Collectors.toList());
    }

    protected List<Subtask> getSubtasks(int count, Epic epic) {
        return IntStream.range(1, 1 + count).mapToObj(i -> newSubtask("name" + i, "desc" + i, epic))
                .collect(Collectors.toList());
    }

    @BeforeEach
    public void init() {
        taskManager = taskManager();
        startTimeGenerator = Managers.getDefaultIdGenerator();
    }

    @Test
    void getTasksList() {
        List<Task> tasks = getTasks(5);

        assertEquals(tasks, taskManager.getTasksList());
    }

    @Test
    void getTasksList_empty() {
        assertTrue(taskManager.getTasksList().isEmpty());
    }

    @Test
    void clearTasks() {
        getTasks(100);
        taskManager.clearTasks();

        assertTrue(taskManager.getTasksList().isEmpty());
    }

    @Test
    void getTaskById() {
        List<Task> tasks = getTasks(100);

        tasks.forEach(task -> assertEquals(task, taskManager.getTaskById(task.getId())));
    }

    @Test
    void getTaskById_unknownId() {
        getTasks(100);

        assertNull(taskManager.getTaskById(Long.MAX_VALUE));
    }

    @Test
    void getTaskById_emptyTaskList() {
        assertNull(taskManager.getTaskById(Long.MAX_VALUE));
    }

    @Test
    void addNewTask() {
        Task task = newTask("name", "desc");

        taskManager.addNewTask(task);

        assertEquals(1, taskManager.getTasksList().size());
        assertEquals(task, taskManager.getTasksList().get(0));
    }

    @Test
    void addNewTask_null() {
        assertThrows(NullPointerException.class, () -> taskManager.addNewTask(null));
    }

    @Test
    void addNewTask_intersection() {
        Task task = getTasks(1).get(0);
        Task copyTask = new Task(
                task.getName(),
                task.getDescription(),
                task.getStartTime(),
                task.getDuration()
        );

        Task nullTask = taskManager.addNewTask(copyTask);

        assertNull(nullTask);
    }

    @Test
    void updateTask() {
        Task task = getTasks(1).get(0);

        task.setDuration(Duration.ZERO.plusDays(3));
        task.setStatus(Status.IN_PROGRESS);
        task.setName("newName");
        taskManager.updateTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());
        assertTrue(taskManager.getTasksList().contains(task));
        assertTrue(taskManager.getHistory().contains(task));
        assertEquals(Status.IN_PROGRESS, savedTask.getStatus());
    }

    @Test
    void updateTask_null() {
        Task task = getTasks(1).get(0);

        assertThrows(NullPointerException.class, () -> taskManager.updateTask(null));
        assertTrue(taskManager.getTasksList().contains(task));
        assertEquals(task, taskManager.getTaskById(task.getId()));
    }

    @Test
    void updateTask_empty() {
        Task task = new Task("name1", "desc1", TIME, DURATION);

        taskManager.updateTask(task);

        assertTrue(taskManager.getTasksList().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void removeTaskById() {
        List<Task> tasks = getTasks(10);

        tasks.forEach(task -> {
            if (task.getId() % 2 == 0) {
                taskManager.removeTaskById(task.getId());
            }
        });

        tasks.forEach(task -> {
            switch(task.getId().intValue() % 2) {
                case 0 -> {
                    assertFalse(taskManager.getTasksList().contains(task));
                }
                case 1 -> {
                    assertTrue(taskManager.getTasksList().contains(task));
                }
            }
        });
    }

    @Test
    void removeTaskById_unknownId() {
        getTasks(10);

        assertNull(taskManager.removeTaskById(Long.MAX_VALUE));
    }

    @Test
    void getSubtasksList() {
        Epic epic = getEpics(1).get(0);
        List<Subtask> subtasks = getSubtasks(10, epic);

        assertEquals(subtasks, taskManager.getSubtasksList());
    }

    @Test
    void getSubtasksList_empty() {
        assertTrue(taskManager.getSubtasksList().isEmpty());
    }

    @Test
    void clearSubtasks() {
        Epic epic = getEpics(1).get(0);
        getSubtasks(100, epic);
        taskManager.clearSubtasks();

        assertTrue(taskManager.getTasksList().isEmpty());
    }

    @Test
    void getSubtasksById() {
        Epic epic = getEpics(1).get(0);
        List<Subtask> subtasks = getSubtasks(100, epic);

        subtasks.forEach(subtask -> assertEquals(subtask, taskManager.getSubtaskById(subtask.getId())));
    }

    @Test
    void getSubtasksById_unknownId() {
        Epic epic = getEpics(1).get(0);
        getSubtasks(100, epic);

        assertNull(taskManager.getSubtaskById(Long.MAX_VALUE));
    }

    @Test
    void getSubtasksById_emptyTaskList() {
        assertNull(taskManager.getSubtaskById(Long.MAX_VALUE));
    }

    @Test
    void addNewSubtask() {
        Subtask subtask = newSubtask("name", "desc", newEpic("name", "desc"));

        taskManager.addNewSubtask(subtask);

        assertEquals(1, taskManager.getSubtasksList().size());
        assertEquals(subtask, taskManager.getSubtasksList().get(0));
    }

    @Test
    void addNewSubtask_null() {
        assertThrows(NullPointerException.class, () -> taskManager.addNewSubtask(null));
    }

    @Test
    void addNewSubtask_intersection() {
        Subtask subtask = newSubtask("name", "desc", newEpic("name", "desc"));
        Subtask copySubtask = new Subtask(
                subtask.getName(),
                subtask.getDescription(),
                subtask.getParentEpic(),
                subtask.getStartTime(),
                subtask.getDuration()
        );

        Subtask nullSubtask = taskManager.addNewSubtask(copySubtask);

        assertNull(nullSubtask);
    }

    @Test
    void updateSubtask() {
        Subtask subtask = newSubtask("name", "desc", newEpic("name", "desc"));
        taskManager.addNewSubtask(subtask);

        subtask.setDuration(Duration.ZERO.plusDays(3));
        subtask.setStatus(Status.IN_PROGRESS);
        subtask.setName("newName");
        taskManager.updateSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtaskById(subtask.getId());
        assertTrue(taskManager.getSubtasksList().contains(subtask));
        assertTrue(taskManager.getHistory().contains(subtask));
        assertEquals(Status.IN_PROGRESS, savedSubtask.getStatus());
    }

    @Test
    void updateSubtask_null() {
        Subtask subtask = newSubtask("name", "desc", newEpic("name", "desc"));
        taskManager.addNewSubtask(subtask);

        assertThrows(NullPointerException.class, () -> taskManager.updateSubtask(null));
        assertTrue(taskManager.getSubtasksList().contains(subtask));
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void updateSubtask_empty() {
        Subtask subtask = newSubtask("name", "desc", newEpic("name", "desc"));

        taskManager.updateSubtask(subtask);

        assertTrue(taskManager.getTasksList().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void removeSubtaskById() {
        Epic epic = getEpics(1).get(0);
        List<Subtask> subtasks = getSubtasks(10, epic);

        subtasks.forEach(subtask -> {
            if (subtask.getId() % 2 == 0) {
                taskManager.removeSubtaskById(subtask.getId());
            }
        });

        subtasks.forEach(subtask -> {
            switch(subtask.getId().intValue() % 2) {
                case 0 -> {
                    assertFalse(taskManager.getSubtasksList().contains(subtask));
                }
                case 1 -> {
                    assertTrue(taskManager.getSubtasksList().contains(subtask));
                }
            }
        });
    }

    @Test
    void removeSubtaskById_unknownId() {
        Epic epic = getEpics(1).get(0);
        getSubtasks(10, epic);

        assertNull(taskManager.removeSubtaskById(Long.MAX_VALUE));
    }

    @Test
    void getEpicsList() {
        List<Epic> epics = getEpics(100);

        assertEquals(epics, taskManager.getEpicsList());
    }

    @Test
    void getEpicsList_empty() {
        assertTrue(taskManager.getEpicsList().isEmpty());
    }

    @Test
    void clearEpics() {
        getEpics(100);
        taskManager.clearEpics();

        assertTrue(taskManager.getEpicsList().isEmpty());
    }

    @Test
    void getEpicById() {
        List<Epic> epics = getEpics(100);

        epics.forEach(epic -> assertEquals(epic, taskManager.getEpicById(epic.getId())));
    }

    @Test
    void getEpicById_unknownId() {
        getEpics(100);

        assertNull(taskManager.getEpicById(Long.MAX_VALUE));
    }

    @Test
    void getEpicById_emptyEpicList() {
        assertNull(taskManager.getEpicById(Long.MAX_VALUE));
    }

    @Test
    void addNewEpic() {
        Epic epic = newEpic("name", "desc");

        assertEquals(1, taskManager.getEpicsList().size());
        assertEquals(epic, taskManager.getEpicsList().get(0));
    }

    @Test
    void addNewEpic_null() {
        assertThrows(NullPointerException.class, () -> taskManager.addNewEpic(null));
    }

    @Test
    void updateEpic() {
        Epic epic = newEpic("name", "desc");
        taskManager.addNewEpic(epic);

        getSubtasks(10, epic);
        taskManager.updateEpic(epic);

        assertTrue(taskManager.getEpicsList().contains(epic));
    }

    @Test
    void updateEpic_null() {
        getEpics(100);

        assertThrows(NullPointerException.class, () -> taskManager.updateEpic(null));
    }

    @Test
    void updateEpic_empty() {
        Epic epic = newEpic("name", "desc");

        taskManager.updateEpic(epic);

        assertTrue(taskManager.getSubtasksList().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void removeEpicById() {
        List<Epic> epics = getEpics(100);

        epics.forEach(epic -> {
            if (epic.getId() % 2 == 0) {
                taskManager.removeEpicById(epic.getId());
            }
        });

        epics.forEach(epic -> {
            switch(epic.getId().intValue() % 2) {
                case 0 -> {
                    assertFalse(taskManager.getEpicsList().contains(epic));
                }
                case 1 -> {
                    assertTrue(taskManager.getEpicsList().contains(epic));
                }
            }
        });
    }

    @Test
    void removeEpicById_unknownId() {
        getEpics(100);

        assertNull(taskManager.removeEpicById(Long.MAX_VALUE));
    }

    @Test
    void getSubtasksByEpicId() {
        List<Epic> epics = getEpics(2);
        List<Subtask> epic1subtasks = getSubtasks(50, epics.get(0));
        List<Subtask> epic2subtasks = getSubtasks(50, epics.get(1));

        assertEquals(epic1subtasks, taskManager.getSubtasksByEpicId(epics.get(0).getId()));
        assertEquals(epic2subtasks, taskManager.getSubtasksByEpicId(epics.get(1).getId()));
    }

    @Test
    void getSubtasksByEpicId_unknownId() {
        List<Epic> epics = getEpics(2);
        getSubtasks(50, epics.get(0));
        getSubtasks(50, epics.get(1));

        assertNull(taskManager.getSubtasksByEpicId(Long.MAX_VALUE));
    }
    @Test
    void getHistory() {
        List<Task> tasks = getTasks(2);
        List<Epic> epics = getEpics(2);
        List<Subtask> epic1subtask = getSubtasks(3, epics.get(0));
        List<Subtask> epic2subtask = getSubtasks(2, epics.get(1));
        epics.forEach(epic -> taskManager.getEpicById(epic.getId()));
        tasks.forEach(task -> taskManager.getTaskById(task.getId()));
        Stream.of(epic1subtask, epic2subtask).flatMap(List::stream)
                .forEach(subtask -> taskManager.getSubtaskById(subtask.getId()));

        Stream.of(epics, epic1subtask, epic2subtask, tasks).flatMap(List::stream)
                .forEach(task -> assertTrue(taskManager.getHistory().contains(task)));
        List<AbstractTask> allTasks = Stream.of(epics, epic1subtask, epic2subtask, tasks)
                .flatMap(List::stream).collect(Collectors.toList());
        List<AbstractTask> history = taskManager.getHistory();
        allTasks.sort(Comparator.comparingLong(AbstractTask::getId));
        history.sort(Comparator.comparingLong(AbstractTask::getId));
        assertEquals(allTasks, history);
    }

    @Test
    void getPrioritizedTasks() {
        List<Task> tasks = getTasks(1000);

        Collections.shuffle(tasks);
        taskManager.clearTasks();
        tasks.forEach(taskManager::addNewTask);

        tasks.sort(Comparator.comparing(Task::getStartTime));
        assertEquals(tasks, taskManager.getPrioritizedTasks());
    }
}
