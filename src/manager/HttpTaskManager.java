package manager;

import manager.exception.ManagerLoadException;
import server.KVTaskClient;
import task.AbstractTimeTask;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient client;
    private final String key;
    private static final Path defaultPath = Path.of("tmp");

    public HttpTaskManager(URI uri) throws IOException, InterruptedException {
        this(uri, UUID.randomUUID().toString());
    }

    public HttpTaskManager(URI uri, String key) throws IOException, InterruptedException {
        super(defaultPath);
        this.client = new KVTaskClient(uri);
        this.key = key;
    }

    public HttpTaskManager(
            URI uri,
            String key,
            Path data,
            HistoryManager history,
            HashMap<Long, Task> tasks,
            HashMap<Long, Epic> epics,
            HashMap<Long, Subtask> subtasks,
            TreeSet<AbstractTimeTask> orderedTasks
    ) {
        super(data, history, tasks, epics, subtasks, orderedTasks);
        try {
            this.client = new KVTaskClient(uri);
            this.key = key;
        } catch (IOException | InterruptedException exception) {
            throw new ManagerLoadException(exception);
        }
    }

    @Override
    protected void save() {
        super.save();
        try (BufferedReader reader = Files.newBufferedReader(data)) {
            client.put(key, reader.lines().collect(Collectors.joining(System.lineSeparator())));
        } catch (IOException | InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static HttpTaskManager loadFromServer(URI uri, String key) throws IOException, InterruptedException {
        try (BufferedReader reader = new BufferedReader(
                new StringReader(new KVTaskClient(uri).load(key)))
        ) {
            return loadFromReader(
                    (h, ts, es, sts, ot) -> new HttpTaskManager(uri, key, defaultPath, h, ts, es, sts, ot),
                    reader
            );
        } catch (IOException | SecurityException exception) {
            throw new RuntimeException(exception);
        }
    }

    public String loadState() throws IOException, InterruptedException {
        return client.load(key);
    }
}
