package manager;

import task.*;

import java.util.List;

public interface TaskManager {
    List<Task> getTasksList();

    void clearTasks();

    Task getTaskById(Long id);

    Task addNewTask(Task task);

    Task updateTask(Task task);

    Task removeTaskById(Long id);


    List<Subtask> getSubtasksList();

    void clearSubtasks();

    Subtask getSubtaskById(Long id);

    Subtask addNewSubtask(Subtask subtask);

    Subtask updateSubtask(Subtask subtask);

    Subtask removeSubtaskById(Long id);


    List<Epic> getEpicsList();

    void clearEpics();

    Epic getEpicById(Long id);

    Epic addNewEpic(Epic epic);

    Epic updateEpic(Epic epic);

    Epic removeEpicById(Long id);

    List<Subtask> getSubtasksByEpicId(Long id);

    List<AbstractTask> getHistory();

    List<AbstractTimeTask> getPrioritizedTasks();
}
