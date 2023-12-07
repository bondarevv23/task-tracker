package server.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import server.controller.adapter.DurationTypeAdapter;
import server.controller.adapter.LocalDateTimeTypeAdapter;
import server.controller.adapter.SubtaskTypeAdapter;
import server.exception.WrongFormatRequestException;
import task.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import static server.Code.*;

public abstract class Controller {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(Subtask.class, new SubtaskTypeAdapter())
            .create();

    private final HttpExchange httpExchange;
    protected final TaskManager taskManager;

    Controller(HttpExchange httpExchange, TaskManager taskManager) {
        this.httpExchange = httpExchange;
        this.taskManager = taskManager;
    }

    protected void okJson(Object object) throws IOException {
        final byte[] responseBody = gson.toJson(object).getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(OK.code, responseBody.length);
        httpExchange.getResponseBody().write(responseBody);
    }

    protected void expectJson() {
        if (!httpExchange.getRequestHeaders().get("Content-Type").contains("application/json")) {
            throw new WrongFormatRequestException("json format expected");
        }
    }

    protected <T> T parseJsonBody(Class<T> clazz) throws IOException {
        final String requestBody = new String(
                httpExchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);
        return gson.fromJson(requestBody, clazz);
    }

    protected void created() throws IOException {
        httpExchange.sendResponseHeaders(CREATED.code, 0);
    }

    protected void noContent() throws IOException {
        httpExchange.sendResponseHeaders(NO_CONTENT.code, -1);
    }

    public static Gson getGson() {
        return gson;
    }
}
