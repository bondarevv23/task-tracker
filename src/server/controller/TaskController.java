package server.controller;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import server.annotation.DELETE;
import server.annotation.GET;
import server.annotation.POST;
import server.annotation.QueryParams;
import task.Task;

import java.io.IOException;
import java.util.Map;

public class TaskController extends Controller {
    public TaskController(HttpExchange httpExchange, TaskManager taskManager) {
        super(httpExchange, taskManager);
    }

    @GET
    public void getAllTasks() throws IOException {
        okJson(taskManager.getTasksList());
    }

    @GET
    @QueryParams(params = {"id"})
    public void getTaskById(Map<String, String> params)
            throws IOException {
        final Task task = taskManager.getTaskById(Long.parseLong(params.get("id")));
        okJson(task);
    }

    @POST
    public void addNewTask() throws IOException {
        expectJson();
        final Task task = parseJsonBody(Task.class);
        taskManager.addNewTask(task);
        created();
    }

    @POST
    @QueryParams(params = {"id"})
    public void updateTask(Map<String, String> params)
            throws IOException {
        expectJson();
        final Task task = parseJsonBody(Task.class);
        task.setId(Long.parseLong(params.get("id")));
        taskManager.updateTask(task);
        created();
    }

    @DELETE
    public void deleteAllTasks() throws IOException {
        taskManager.clearTasks();
        noContent();
    }

    @DELETE
    @QueryParams(params = {"id"})
    public void deleteTaskById(Map<String, String> params)
            throws IOException {
        final Long id = Long.parseLong(params.get("id"));
        taskManager.removeTaskById(id);
        noContent();
    }
}
