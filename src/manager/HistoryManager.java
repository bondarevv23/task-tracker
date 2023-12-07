package manager;

import task.AbstractTask;

import java.util.List;

public interface HistoryManager {
    void add(AbstractTask task);

    void remove(long id);

    List<AbstractTask> getHistory();
}
