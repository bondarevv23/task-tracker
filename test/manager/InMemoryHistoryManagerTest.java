package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.AbstractTask;
import task.Epic;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private IdGenerator generator;
    private final LocalDateTime time = LocalDateTime.of(1, 1, 1, 0, 0, 0);
    private final Duration duration = Duration.ZERO;

    @BeforeEach
    public void init() {
        historyManager = new InMemoryHistoryManager();
        generator = Managers.getDefaultIdGenerator();
    }

    private Task newTask(String name, String desc) {
        Task task = new Task(name, desc, time, duration);
        task.setId(generator.generateId());
        return task;
    }

    private Epic newEpic(String name, String desc) {
        Epic epic = new Epic(name, desc);
        epic.setId(generator.generateId());
        return epic;
    }

    private Subtask newSubtask(String name, String desc, Epic epic) {
        Subtask subtask = new Subtask(name, desc, epic, time, duration);;
        subtask.setId(generator.generateId());
        return subtask;
    }

    @Test
    void add() {
        List<Task> tasks = List.of(
                newTask("name1", "desc1"),
                newTask("name2", "desc2")
        );
        List<Epic> epics = List.of(
                newEpic("name1", "desc1"),
                newEpic("name2", "desc2")
        );
        List<Subtask> subtasks = List.of(
                newSubtask("name1", "desc1", epics.get(0)),
                newSubtask("name2", "desc2", epics.get(0)),
                newSubtask("name3", "desc3", epics.get(1))
        );

        epics.forEach(historyManager::add);
        tasks.forEach(historyManager::add);
        subtasks.forEach(historyManager::add);

        List<AbstractTask> myHistory = new ArrayList<>();
        myHistory.addAll(epics);
        myHistory.addAll(tasks);
        myHistory.addAll(subtasks);
        Collections.reverse(myHistory);

        List<AbstractTask> history = historyManager.getHistory();
        assertEquals(myHistory.size(), history.size());
        for (int i = 0; i < history.size(); i++) {
            assertEquals(myHistory.get(i), history.get(i));
        }
    }

    @Test
    void add_duplicates() {
        List<Task> tasks = List.of(
                newTask("name1", "desc1"),
                newTask("name2", "desc2")
        );

        tasks.forEach(historyManager::add);
        tasks.forEach(historyManager::add);

        List<AbstractTask> myHistory = new ArrayList<>(tasks);
        Collections.reverse(myHistory);
        List<AbstractTask> history = historyManager.getHistory();

        assertEquals(tasks.size(), history.size());
        for (int i = 0; i < history.size(); i++) {
            assertEquals(myHistory.get(i), history.get(i));
        }
    }

    @Test
    void add_upperBound() {
        List<Task> tasks = List.of(
                newTask("name1", "desc1"),
                newTask("name2", "desc2"),
                newTask("name3", "desc3"),
                newTask("name4", "desc4"),
                newTask("name5", "desc5"),
                newTask("name6", "desc6"),
                newTask("name7", "desc7"),
                newTask("name8", "desc8"),
                newTask("name9", "desc9"),
                newTask("name10", "desc10"),
                newTask("name11", "desc11"),
                newTask("name12", "desc12")
        );

        tasks.forEach(historyManager::add);

        List<AbstractTask> myHistory = new ArrayList<>();
        myHistory.addAll(tasks.subList(2, 12));
        Collections.reverse(myHistory);

        List<AbstractTask> history = historyManager.getHistory();
        assertEquals(myHistory.size(), history.size());
        for (int i = 0; i < history.size(); i++) {
            assertEquals(myHistory.get(i), history.get(i));
        }
    }

    @Test
    void getHistory_empty() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void remove_begin() {
        List<Task> tasks = List.of(
                newTask("name1", "desc1"),
                newTask("name2", "desc2")
        );
        List<Epic> epics = List.of(
                newEpic("name1", "desc1"),
                newEpic("name2", "desc2")
        );
        List<Subtask> subtasks = List.of(
                newSubtask("name1", "desc1", epics.get(0)),
                newSubtask("name2", "desc2", epics.get(0)),
                newSubtask("name3", "desc3", epics.get(1))
        );

        tasks.forEach(historyManager::add);
        epics.forEach(historyManager::add);
        subtasks.forEach(historyManager::add);

        historyManager.remove(subtasks.get(2).getId());

        List<AbstractTask> history = historyManager.getHistory();
        List<AbstractTask> myHistory = new ArrayList<>();
        myHistory.addAll(tasks);
        myHistory.addAll(epics);
        myHistory.addAll(subtasks.subList(0, 2));
        Collections.reverse(myHistory);

        assertFalse(history.contains(subtasks.get(2)));
        for (int i = 0; i < history.size(); i++) {
            assertEquals(myHistory.get(i), history.get(i));
        }
    }

    @Test
    void remove_middle() {
        List<Task> tasks = List.of(
                newTask("name1", "desc1"),
                newTask("name2", "desc2")
        );
        List<Epic> epics = List.of(
                newEpic("name1", "desc1"),
                newEpic("name2", "desc2")
        );
        List<Subtask> subtasks = List.of(
                newSubtask("name1", "desc1", epics.get(0)),
                newSubtask("name2", "desc2", epics.get(0)),
                newSubtask("name3", "desc3", epics.get(1))
        );

        tasks.forEach(historyManager::add);
        epics.forEach(historyManager::add);
        subtasks.forEach(historyManager::add);

        historyManager.remove(epics.get(0).getId());

        List<AbstractTask> history = historyManager.getHistory();
        List<AbstractTask> myHistory = new ArrayList<>();
        myHistory.addAll(tasks);
        myHistory.addAll(epics.subList(1, 2));
        myHistory.addAll(subtasks);
        Collections.reverse(myHistory);

        assertFalse(history.contains(epics.get(0)));
        for (int i = 0; i < history.size(); i++) {
            assertEquals(myHistory.get(i), history.get(i));
        }
    }

    @Test
    void remove_last() {
        List<Task> tasks = List.of(
                newTask("name1", "desc1"),
                newTask("name2", "desc2")
        );
        List<Epic> epics = List.of(
                newEpic("name1", "desc1"),
                newEpic("name2", "desc2")
        );
        List<Subtask> subtasks = List.of(
                newSubtask("name1", "desc1", epics.get(0)),
                newSubtask("name2", "desc2", epics.get(0)),
                newSubtask("name3", "desc3", epics.get(1))
        );

        tasks.forEach(historyManager::add);
        epics.forEach(historyManager::add);
        subtasks.forEach(historyManager::add);

        historyManager.remove(tasks.get(0).getId());

        List<AbstractTask> history = historyManager.getHistory();
        List<AbstractTask> myHistory = new ArrayList<>();
        myHistory.addAll(tasks.subList(1, 2));
        myHistory.addAll(epics);
        myHistory.addAll(subtasks);
        Collections.reverse(myHistory);

        assertFalse(history.contains(tasks.get(0)));
        for (int i = 0; i < history.size(); i++) {
            assertEquals(myHistory.get(i), history.get(i));
        }
    }
}
