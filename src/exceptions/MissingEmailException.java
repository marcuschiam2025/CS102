package exceptions;

public class MissingEmailException extends RuntimeException {
    public MissingEmailException(String message) {
        super(message);
    }
}
