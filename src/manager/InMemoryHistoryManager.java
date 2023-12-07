package manager;

import task.AbstractTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList<AbstractTask> history;
    private final Map<Long, CustomLinkedList.Node<AbstractTask>> id2node;

    public InMemoryHistoryManager() {
        history = new CustomLinkedList<>(10);
        id2node = new HashMap<>();
    }

    @Override
    public void add(AbstractTask task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        CustomLinkedList.Node<AbstractTask> node = history.linkLast(task);
        id2node.put(task.getId(), node);
    }

    @Override
    public void remove(long id) {
        CustomLinkedList.Node<AbstractTask> node = id2node.get(id);
        if (node == null) {
            return;
        }
        history.removeNode(node);
        id2node.remove(id);
    }

    @Override
    public List<AbstractTask> getHistory() {
        return history.getList();
    }
}
