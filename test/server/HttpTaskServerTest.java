package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;
import task.Epic;
import task.Subtask;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import server.controller.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static server.controller.adapter.SubtaskTypeAdapter.EMPTY_EPIC;

public class HttpTaskServerTest {
    private KVServer kvServer;
    private HttpTaskServer httpTaskServer;

    @BeforeEach
    public void startServer() throws IOException, InterruptedException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
    }

    @AfterEach
    public void stopServer() {
        httpTaskServer.stop();
        kvServer.stop();
    }

    @Test
    public void testHttpAndGson() throws IOException, InterruptedException {
        LocalDateTime time = LocalDateTime.of(1, 1, 1, 0, 0, 0);
        Duration duration = Duration.ZERO;
        List<Task> tasks = List.of(
                new Task("name1", "desc1", time, duration),
                new Task("name2", "desc2", time, duration),
                new Task("name3", "desc3", time, duration)
        );
        List<Epic> epics = List.of(
                new Epic("name1", "desc1"),
                new Epic("name2", "desc2"),
                new Epic("name3", "desc3")
        );
        List<Subtask> subtasks = List.of(
                new Subtask("name1", "desc1", epics.get(0), time, duration),
                new Subtask("name2", "desc2", epics.get(0), time, duration),
                new Subtask("name3", "desc3", epics.get(0), time, duration),
                new Subtask("name2", "desc2", epics.get(1), time, duration),
                new Subtask("name2", "desc2", epics.get(1), time, duration)
        );
        for (int i = 1; i <= tasks.size(); i++) {
            tasks.get(i - 1).setId((long) i);
        }
        for (int i = 1; i <= epics.size(); i++) {
            epics.get(i - 1).setId((long) (tasks.size() + i));
        }
        for (int i = 1; i <= subtasks.size(); i++) {
            subtasks.get(i - 1).setId((long) (tasks.size() + epics.size() + i));
        }
        URI uriTask = URI.create("http://localhost:8080/tasks/task/");
        URI uriEpic = URI.create("http://localhost:8080/epics/epic/");
        URI uriSubtask = URI.create("http://localhost:8080/subtasks/subtask/");
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        Gson gson = Controller.getGson();
        Type listOfTask = new TypeToken<ArrayList<Task>>() {}.getType();
        Type listOfEpic = new TypeToken<ArrayList<Epic>>() {}.getType();
        Type listOfSubtask = new TypeToken<ArrayList<Subtask>>() {}.getType();

        tasks.forEach(task -> {
            String json = gson.toJson(task);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uriTask)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();
            try {
                client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        epics.forEach(epic -> {
            String json = gson.toJson(epic);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uriEpic)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();
            try {
                client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        subtasks.forEach(subtask -> {
            String json = gson.toJson(subtask);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uriSubtask)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();
            try {
                client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(
                tasks,
                gson.fromJson(client.send(
                        HttpRequest.newBuilder().uri(uriTask).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                ).body(), listOfTask)
        );
        assertEquals(
                epics,
                gson.fromJson(client.send(
                        HttpRequest.newBuilder().uri(uriEpic).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                ).body(), listOfEpic)
        );
        subtasks.forEach(subtask -> subtask.setParentEpic(EMPTY_EPIC));
        assertEquals(
                subtasks,
                gson.fromJson(client.send(
                        HttpRequest.newBuilder().uri(uriSubtask).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                ).body(), listOfSubtask)
        );
    }
}
