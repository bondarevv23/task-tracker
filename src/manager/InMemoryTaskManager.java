package manager;

import task.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final IdGenerator generator;
    protected final HistoryManager history;

    private final HashMap<Long, Task> tasks;
    private final HashMap<Long, Epic> epics;
    private final HashMap<Long, Subtask> subtasks;
    private final TreeSet<AbstractTimeTask> orderedTasks;

    public InMemoryTaskManager() {
        this(Managers.getDefaultHistory(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
                new TreeSet<>(Comparator.comparing(AbstractTimeTask::getStartTime)));
    }

    InMemoryTaskManager(
            HistoryManager history,
            HashMap<Long, Task> tasks,
            HashMap<Long, Epic> epics,
            HashMap<Long, Subtask> subtasks,
            TreeSet<AbstractTimeTask> orderedTasks
    ) {
        this.generator = Managers.getDefaultIdGenerator();
        this.history = history;
        this.tasks = tasks;
        this.epics = epics;
        this.subtasks = subtasks;
        this.orderedTasks = orderedTasks;
    }

    @Override
    public List<Task> getTasksList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void clearTasks() {
        tasks.forEach((i, task) -> orderedTasks.remove(task));
        tasks.clear();
    }

    @Override
    public Task getTaskById(Long id) {
        Task task = tasks.get(id);
        addToHistoryIfNotNull(task);
        return task;
    }

    @Override
    public Task addNewTask(Task task) {
        if (intersections(task)) {
            return null;
        }
        orderedTasks.add(task);
        task.setId(generator.generateId());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        Task currentTask = tasks.get(task.getId());
        if (currentTask == null) {
            return null;
        }
        orderedTasks.remove(currentTask);
        if (intersections(task)) {
            orderedTasks.add(currentTask);
            return null;
        }
        Task t = tasks.put(task.getId(), task);
        orderedTasks.add(t);
        return t;
    }

    @Override
    public Task removeTaskById(Long id) {
        history.remove(id);
        Task removed = tasks.remove(id);
        if (removed == null) {
            return null;
        }
        orderedTasks.remove(removed);
        return removed;
    }


    @Override
    public List<Subtask> getSubtasksList() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearSubtasks() {
        subtasks.forEach((i, subtask) -> orderedTasks.remove(subtask));
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clear();
        }
    }

    @Override
    public Subtask getSubtaskById(Long id) {
        Subtask subtask = subtasks.get(id);
        addToHistoryIfNotNull(subtask);
        return subtask;
    }

    @Override
    public Subtask addNewSubtask(Subtask subtask) {
        if (intersections(subtask)) {
            return null;
        }
        subtask.setId(generator.generateId());
        orderedTasks.add(subtask);
        subtasks.put(subtask.getId(), subtask);
        updateStatus(subtask.getParentEpic());
        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask currentSubtask = subtasks.get(subtask.getId());
        if (currentSubtask == null) {
            return null;
        }
        orderedTasks.remove(currentSubtask);
        if (intersections(subtask)) {
            orderedTasks.add(currentSubtask);
            return null;
        }
        updateStatus(subtask.getParentEpic());
        Subtask s = subtasks.put(subtask.getId(), subtask);
        orderedTasks.add(s);
        return s;
    }

    @Override
    public Subtask removeSubtaskById(Long id) {
        history.remove(id);
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            return null;
        }
        orderedTasks.remove(subtask);
        subtask.getParentEpic().getSubtasks().remove(subtask);
        updateStatus(subtask.getParentEpic());
        return subtask;
    }


    @Override
    public List<Epic> getEpicsList() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void clearEpics() {
        epics.clear();
        subtasks.forEach((i, subtask) -> orderedTasks.remove(subtask));
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(Long id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        epic.getSubtasks().forEach(this::addToHistoryIfNotNull);
        addToHistoryIfNotNull(epic);
        return epic;
    }

    @Override
    public Epic addNewEpic(Epic epic) {
        epic.setId(generator.generateId());
        updateStatus(epic);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic currentEpic = epics.get(epic.getId());
        if (currentEpic == null) {
            return null;
        }
        updateStatus(epic);
        return epics.put(epic.getId(), epic);
    }

    @Override
    public Epic removeEpicById(Long id) {
        history.remove(id);
        Epic epic = epics.remove(id);
        if (epic == null) {
            return null;
        }
        List<Subtask> subtasks = epic.getSubtasks();
        while (!subtasks.isEmpty()) {
            removeSubtaskById(subtasks.get(subtasks.size() - 1).getId());
        }
        return epic;
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(Long id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        return epic.getSubtasks();
    }

    @Override
    public List<AbstractTask> getHistory() {
        return history.getHistory();
    }

    public List<AbstractTimeTask> getPrioritizedTasks() {
        return new ArrayList<>(orderedTasks);
    }

    private void updateStatus(Epic epic) {
        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        if (epic.getSubtasks().stream().allMatch(subtask -> subtask.getStatus().equals(Status.NEW))) {
            epic.setStatus(Status.NEW);
            return;
        }
        if (epic.getSubtasks().stream().allMatch(subtask -> subtask.getStatus().equals(Status.DONE))) {
            epic.setStatus(Status.DONE);
            return;
        }
        epic.setStatus(Status.IN_PROGRESS);
    }

    private void addToHistoryIfNotNull(AbstractTask task) {
        if (task != null) {
            history.add(task);
        }
    }

    private boolean intersections(AbstractTimeTask task) {
        for (AbstractTimeTask ptr : orderedTasks) {
            if (task.getStartTime().isBefore(ptr.getEndTime()) && ptr.getStartTime().isBefore(task.getEndTime())) {
                return true;
            }
        }
        return false;
    }
}
