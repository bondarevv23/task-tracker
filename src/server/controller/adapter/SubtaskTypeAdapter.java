package server.controller.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import task.Epic;
import task.Status;
import task.Subtask;

import java.io.IOException;

public class SubtaskTypeAdapter extends TypeAdapter<Subtask> {
    public static final Epic EMPTY_EPIC = new Epic("", "");
    static {
        EMPTY_EPIC.setId(0L);
    }

    @Override
    public void write(JsonWriter jsonWriter, Subtask subtask) throws IOException {
        if (subtask == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.beginObject();
        jsonWriter.name("id").value(subtask.getId());
        jsonWriter.name("name").value(subtask.getName());
        jsonWriter.name("description").value(subtask.getDescription());
        jsonWriter.name("status").value(subtask.getStatus().name());
        jsonWriter.name("start_time");
        new LocalDateTimeTypeAdapter().write(jsonWriter, subtask.getStartTime());
        jsonWriter.name("duration");
        new DurationTypeAdapter().write(jsonWriter, subtask.getDuration());
        jsonWriter.name("epic").value(subtask.getParentEpic().getId());
        jsonWriter.endObject();
    }

    @Override
    public Subtask read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        Subtask.SubtaskBuilder builder = Subtask.builder();
        Long id = null;
        Status status = null;
        jsonReader.beginObject();
        while (jsonReader.peek() != JsonToken.END_OBJECT) {
            switch (jsonReader.nextName()) {
                case "id" -> id = Long.parseLong(jsonReader.nextString());
                case "name" -> builder.name(jsonReader.nextString());
                case "description" -> builder.description(jsonReader.nextString());
                case "status" -> status = Status.valueOf(jsonReader.nextString());
                case "start_time" -> builder.startTime(new LocalDateTimeTypeAdapter().read(jsonReader));
                case "duration" -> builder.duration(new DurationTypeAdapter().read(jsonReader));
                case "epic" -> {
                    builder.epic(EMPTY_EPIC);
                    jsonReader.nextString();
                }
            }
        }
        jsonReader.endObject();
        final Subtask subtask = builder.build();
        subtask.setId(id);
        subtask.setStatus(status);
        return subtask;
    }
}
