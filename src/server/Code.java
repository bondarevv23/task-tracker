package server;

public enum Code {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);

    Code(int code) {
        this.code = code;
    }

    public final int code;
}
