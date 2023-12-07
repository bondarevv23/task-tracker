package manager.exception;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(Exception exception) {
        super(exception);
    }
}
