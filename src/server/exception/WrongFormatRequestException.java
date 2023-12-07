package server.exception;

public class WrongFormatRequestException extends RuntimeException {
    public WrongFormatRequestException(String message) {
        super(message);
    }
}
