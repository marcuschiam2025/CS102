package exceptions;

public class MissingFileException extends RuntimeException{
    public  MissingFileException(String s){
        super(s);
    }
}
