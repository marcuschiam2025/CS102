package ThrowException;

public class MissingNameException extends RuntimeException {
    public  MissingNameException(String s){
        super(s);
    }
}
