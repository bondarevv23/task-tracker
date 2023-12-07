package server.exception;

public class ServerAccessControllerException extends RuntimeException {
    public ServerAccessControllerException(Exception exception) {
        super(exception);
    }
}
