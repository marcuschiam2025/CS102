package ThrowException;
import java.util.regex.Pattern;

// import OwnExceptions.FolderNameException;

public class FolderNameChecker {
    public static boolean FolderName(String filepath){
        String[] names = filepath.split("\\\\");

        if(Pattern.matches("^[A-Za-z]+[.][0-9]{4}$", names[1]) || Pattern.matches("^[A-Za-z]+[.][A-Za-z]+[.][0-9]{4}$", names[1])){
            System.out.println(filepath + " Folder name correct");
            return  true;
        }else{
            return  false;
            // throw new FolderNameException("Folder name is incorrect");
        }
    }
}
