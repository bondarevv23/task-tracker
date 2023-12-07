package manager;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class Managers {
    public static TaskManager getDefault() throws IOException, InterruptedException {
        return new HttpTaskManager(URI.create("http://localhost:8078"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static IdGenerator getDefaultIdGenerator() {
        return new SequentialIdGenerator();
    }

    public static TaskManager getFiled(Path path) {
        return new FileBackedTasksManager(path);
    }
}
