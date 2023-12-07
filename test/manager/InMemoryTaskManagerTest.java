package manager;

public class InMemoryTaskManagerTest extends AbstractTaskManagerTest{
    @Override
    TaskManager taskManager() {
        return new InMemoryTaskManager();
    }
}