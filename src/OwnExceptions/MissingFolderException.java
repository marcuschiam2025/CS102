package OwnExceptions;

public class MissingFolderException extends RuntimeException {
    public MissingFolderException(String message) {
        super(message);
    }
}
