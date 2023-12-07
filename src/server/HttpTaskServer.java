package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;
import server.annotation.DELETE;
import server.annotation.GET;
import server.annotation.POST;
import server.annotation.QueryParams;
import server.controller.Controller;
import server.controller.EpicController;
import server.controller.SubtaskController;
import server.controller.TaskController;
import server.exception.ServerAccessControllerException;
import server.exception.WrongFormatRequestException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static server.Code.*;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Path defaultPath = Path.of("tmp");
    private final TaskManager taskManager;
    private final HttpServer server;
    private static final Map<String, Class<? extends Controller>> CONTROLLERS = Map.of(
            "/tasks/task/", TaskController.class,
            "/epics/epic/", EpicController.class,
            "/subtasks/subtask/", SubtaskController.class
    );
    private static final Map<String, Class<? extends Annotation>> METHODS = Map.of(
            "GET", GET.class,
            "POST", POST.class,
            "DELETE", DELETE.class
    );

    public HttpTaskServer(String hostname) throws IOException, InterruptedException {
        taskManager = Managers.getDefault();
        server = HttpServer.create(new InetSocketAddress(hostname, PORT), 0);
        for (Map.Entry<String, Class<? extends Controller>> entry : CONTROLLERS.entrySet()) {
            server.createContext(entry.getKey(), getHandler(entry.getValue()));
        }
        server.createContext("/", HttpTaskServer::notFound);
    }

    public HttpTaskServer() throws IOException, InterruptedException {
        this("localhost");
    }

    private Map<String, String> parseQuery(String query) {
        return Optional.ofNullable(query).map(str -> Arrays.stream(str.split("&"))
                .collect(Collectors.toMap(
                        s -> s.substring(0, s.indexOf('=')),
                        s -> s.substring(s.indexOf('=') + 1))))
                .orElse(Collections.emptyMap());
    }

    private HttpHandler getHandler(Class<?> clazz) {
        return (httpExchange) -> {
            try (httpExchange) {
                final Class<? extends Annotation> requestMethodAnnotation = METHODS.get(httpExchange.getRequestMethod());
                if (requestMethodAnnotation == null) {
                    notFound(httpExchange);
                }
                Controller controllerInstance = CONTROLLERS.get(httpExchange.getRequestURI().getPath())
                        .getConstructor(HttpExchange.class, TaskManager.class)
                        .newInstance(httpExchange, taskManager);
                final Map<String, String> queryParams = parseQuery(httpExchange.getRequestURI().getQuery());
                for (Method method : clazz.getDeclaredMethods()) {
                    try {
                        if (!method.isAnnotationPresent(requestMethodAnnotation)) {
                            continue;
                        }
                        final QueryParams controllerParams = method.getAnnotation(QueryParams.class);
                        if (controllerParams == null) {
                            if (!queryParams.isEmpty()) {
                                continue;
                            }
                            method.invoke(controllerInstance);
                            return;
                        }
                        final String[] params = controllerParams.params();
                        if (params.length == queryParams.size() &&
                                Arrays.stream(params).allMatch(queryParams::containsKey)) {
                            method.invoke(controllerInstance, queryParams);
                            return;
                        }
                    } catch (IllegalAccessException exception) {
                        throw new ServerAccessControllerException(exception);
                    } catch (InvocationTargetException exception) {
                        internalServerError(httpExchange);
                    } catch (WrongFormatRequestException exception) {
                        badRequest(httpExchange);
                    }
                }
                notFound(httpExchange);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                internalServerError(httpExchange);
            }
        };
    }

    private static void badRequest(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(BAD_REQUEST.code, 0);
    }

    private static void notFound(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(NOT_FOUND.code, 0);
    }

    private static void internalServerError(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(INTERNAL_SERVER_ERROR.code, 0);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(1);
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();
            System.in.read();
            server.stop();
        } catch (IOException | InterruptedException exception) {
            System.err.println(exception.getMessage());
        }
    }
}
