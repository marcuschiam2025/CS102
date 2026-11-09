package exceptions;

public class UnzipErrorException extends RuntimeException{
    public UnzipErrorException(String message) {
        super(message);
    }
}
