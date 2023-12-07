package server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final String apiToken;
    private final URI uri;

    public KVTaskClient(URI serverUri) throws IOException, InterruptedException {
        this.uri = serverUri;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(uri.resolve("/register")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        apiToken = response.body();
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(addApiTokenQuery(uri.resolve("/save/" + key)))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String load(String key) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(addApiTokenQuery(uri.resolve("/load/" + key)))
                .GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private URI addApiTokenQuery(URI uri) {
        return URI.create(uri.toString() + "?API_TOKEN=" + apiToken);
    }

    public static void main(String[] args) {
        try {
            KVTaskClient client = new KVTaskClient(URI.create("http://localhost:8078/"));
            client.put("key1", "value1");
            client.put("key2", "value2");
            System.out.println(client.load("key1"));
            System.out.println(client.load("key2"));
            client.put("key1", "value3");
            System.out.println(client.load("key1"));
            System.out.println(client.load("key4"));
        } catch (IOException | InterruptedException exception) {
            // ignored.
        }
    }
}