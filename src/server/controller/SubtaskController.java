package server.controller;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import server.annotation.DELETE;
import server.annotation.GET;
import server.annotation.POST;
import server.annotation.QueryParams;
import task.Subtask;

import java.io.IOException;
import java.util.Map;

public class SubtaskController extends Controller {
    public SubtaskController(HttpExchange httpExchange, TaskManager taskManager) {
        super(httpExchange, taskManager);
    }

    @GET
    public void getAllSubtasks() throws IOException {
        okJson(taskManager.getSubtasksList());
    }

    @GET
    @QueryParams(params = {"id"})
    public void getSubtaskById(Map<String, String> params)
            throws IOException {
        final Subtask subtask = taskManager.getSubtaskById(Long.parseLong(params.get("id")));
        okJson(subtask);
    }

    @POST
    public void addNewSubtask() throws IOException {
        expectJson();
        final Subtask subtask = parseJsonBody(Subtask.class);
        taskManager.addNewSubtask(subtask);
        created();
    }

    @POST
    @QueryParams(params = {"id"})
    public void updateSubtask(Map<String, String> params)
            throws IOException {
        expectJson();
        final Subtask subtask = parseJsonBody(Subtask.class);
        subtask.setId(Long.parseLong(params.get("id")));
        taskManager.updateSubtask(subtask);
        created();
    }

    @DELETE
    public void deleteAllSubtasks() throws IOException {
        taskManager.clearSubtasks();
        noContent();
    }

    @DELETE
    @QueryParams(params = {"id"})
    public void deleteSubtaskById(Map<String, String> params)
            throws IOException {
        final Long id = Long.parseLong(params.get("id"));
        taskManager.removeSubtaskById(id);
        noContent();
    }
}
