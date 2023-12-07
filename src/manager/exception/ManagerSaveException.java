package manager.exception;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(Exception exception) {
        super(exception);
    }
}
