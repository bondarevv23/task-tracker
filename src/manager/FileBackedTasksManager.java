package manager;

import manager.exception.ManagerLoadException;
import manager.exception.ManagerSaveException;
import manager.functional.FiveFunction;
import task.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class FileBackedTasksManager extends InMemoryTaskManager {

    protected final Path data;

    public FileBackedTasksManager(Path data) {
        super();
        this.data = data;
    }

    public FileBackedTasksManager(
            Path data,
            HistoryManager history,
            HashMap<Long, Task> tasks,
            HashMap<Long, Epic> epics,
            HashMap<Long, Subtask> subtasks,
            TreeSet<AbstractTimeTask> orderedTasks
    ) {
        super(history, tasks, epics, subtasks, orderedTasks);
        this.data = data;
    }

    protected static <T extends TaskManager> T loadFromReader(
            FiveFunction<
                 HistoryManager,
                 HashMap<Long, Task>,
                 HashMap<Long, Epic>,
                 HashMap<Long, Subtask>,
                 TreeSet<AbstractTimeTask>,
                 T> function,
            BufferedReader reader) throws IOException {
        HashMap<Long, Task> tasks = new HashMap<>();
        HashMap<Long, Epic> epics = new HashMap<>();
        HashMap<Long, Subtask> subtasks = new HashMap<>();
        HistoryManager history = Managers.getDefaultHistory();
        String line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            switch (TaskType.valueOf(line.split(CSVFormatter.delimiter)[1])) {
                case TASK -> {
                    Task task = CSVFormatter.parseTask(line);
                    tasks.put(task.getId(), task);
                }
                case EPIC -> {
                    Epic epic = CSVFormatter.parseEpic(line);
                    epics.put(epic.getId(), epic);
                }
                case SUBTASK -> {
                    Subtask subtask = CSVFormatter.parseSubtask(line, epics::get);
                    subtasks.put(subtask.getId(), subtask);
                }
            }
            line = reader.readLine();
        }
        line = reader.readLine();
        if (line != null) {
            List<Long> historyId = CSVFormatter.parseHistory(line);
            for (int i = historyId.size() - 1; i >= 0; i--) {
                long id = historyId.get(i);
                AbstractTask abstractTask = tasks.get(id);
                if (abstractTask != null) {
                    history.add(abstractTask);
                    continue;
                }
                abstractTask = epics.get(id);
                if (abstractTask != null) {
                    history.add(abstractTask);
                    continue;
                }
                history.add(subtasks.get(id));
            }
        }
        TreeSet<AbstractTimeTask> orderedTasks = new TreeSet<>(Comparator.comparing(AbstractTimeTask::getStartTime));
        orderedTasks.addAll(tasks.values());
        orderedTasks.addAll(subtasks.values());
        return function.apply(history, tasks, epics, subtasks, orderedTasks);
    }

    public static FileBackedTasksManager loadFromFile(Path data) {
        try (BufferedReader reader = Files.newBufferedReader(data)) {
            return loadFromReader(
                    (h, ts, es, sts, ot) -> new FileBackedTasksManager(data, h, ts, es, sts, ot),
                    reader);
        } catch (IOException exception) {
            throw new ManagerLoadException(exception);
        }
    }

    protected void save() {
        try  {
            try (BufferedWriter writer = Files.newBufferedWriter(data)) {
                for (Task task : getTasksList()) {
                    writer.write(CSVFormatter.toString(task));
                    writer.newLine();
                }
                for (Epic epic : getEpicsList()) {
                    writer.write(CSVFormatter.toString(epic));
                    writer.newLine();
                }
                for (Subtask subtask : getSubtasksList()) {
                    writer.write(CSVFormatter.toString(subtask));
                    writer.newLine();
                }
                if (!history.getHistory().isEmpty()) {
                    writer.newLine();
                    writer.write(CSVFormatter.toString(history));
                }
            }
        } catch (IOException | SecurityException exception) {
            throw new ManagerSaveException(exception);
        }
    }

    private <T> T saveWrapper(Supplier<T> method) {
        T t = method.get();
        save();
        return t;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public Task getTaskById(Long id) {
        return saveWrapper(() -> super.getTaskById(id));
    }

    @Override
    public Task addNewTask(Task task) {
        return saveWrapper(() -> super.addNewTask(task));
    }

    @Override
    public Task updateTask(Task task) {
        return saveWrapper(() -> super.updateTask(task));
    }

    @Override
    public Task removeTaskById(Long id) {
        return saveWrapper(() -> super.removeTaskById(id));
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public Subtask getSubtaskById(Long id) {
        return saveWrapper(() -> super.getSubtaskById(id));
    }

    @Override
    public Subtask addNewSubtask(Subtask subtask) {
        return saveWrapper(() -> super.addNewSubtask(subtask));
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        return saveWrapper(() -> super.updateSubtask(subtask));
    }

    @Override
    public Subtask removeSubtaskById(Long id) {
        return saveWrapper(() -> super.removeSubtaskById(id));
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public Epic getEpicById(Long id) {
        return saveWrapper(() -> super.getEpicById(id));
    }

    @Override
    public Epic addNewEpic(Epic epic) {
        return saveWrapper(() -> super.addNewEpic(epic));
    }

    @Override
    public Epic updateEpic(Epic epic) {
        return saveWrapper(() -> super.updateEpic(epic));
    }

    @Override
    public Epic removeEpicById(Long id) {
        return saveWrapper(() -> super.removeEpicById(id));
    }
}
