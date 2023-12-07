package server.controller;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import server.annotation.DELETE;
import server.annotation.GET;
import server.annotation.POST;
import server.annotation.QueryParams;
import task.Epic;

import java.io.IOException;
import java.util.Map;

public class EpicController extends Controller {
    public EpicController(HttpExchange httpExchange, TaskManager taskManager) {
        super(httpExchange, taskManager);
    }

    @GET
    public void getAllEpics() throws IOException {
        okJson(taskManager.getEpicsList());
    }

    @GET
    @QueryParams(params = {"id"})
    public void getEpicById(Map<String, String> params)
            throws IOException {
        final Epic epic = taskManager.getEpicById(Long.parseLong(params.get("id")));
        okJson(epic);
    }

    @POST
    public void addNewEpic() throws IOException {
        expectJson();
        final Epic epic = parseJsonBody(Epic.class);
        taskManager.addNewEpic(epic);
        created();
    }

    @POST
    @QueryParams(params = {"id"})
    public void updateEpic(Map<String, String> params)
            throws IOException {
        expectJson();
        final Epic epic = parseJsonBody(Epic.class);
        epic.setId(Long.parseLong(params.get("id")));
        taskManager.updateEpic(epic);
        created();
    }

    @DELETE
    public void deleteAllEpics() throws IOException {
        taskManager.clearEpics();
        noContent();
    }

    @DELETE
    @QueryParams(params = {"id"})
    public void deleteEpicById(Map<String, String> params)
            throws IOException {
        final Long id = Long.parseLong(params.get("id"));
        taskManager.removeEpicById(id);
        noContent();
    }
}
