package exceptions;

// Thrown when a submission ZIP is empty, corrupt or not a ZIP file at all,
// so there is nothing to check, fix or grade
public class InvalidZipException extends RuntimeException {
    public InvalidZipException(String message) {
        super(message);
    }
}
