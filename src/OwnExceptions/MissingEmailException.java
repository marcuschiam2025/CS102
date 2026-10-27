package OwnExceptions;

public class MissingEmailException extends RuntimeException {
        public  MissingEmailException(String s){
        super(s);
    }
}
