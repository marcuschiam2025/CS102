package ThrowException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

// import OwnExceptions.FolderNameException;

public class FolderNameChecker {
    public static boolean FolderName(String filepath){
        Path path = Paths.get(filepath);

        // Get the folder's name directly
        String folderName = path.getFileName().toString();

        if(Pattern.matches("^[A-Za-z]+[.][0-9]{4}$", folderName) || Pattern.matches("^[A-Za-z]+[.][A-Za-z]+[.][0-9]{4}$", folderName)){
            System.out.println(filepath + " Folder name correct");
            return  true;
        }else{
            return  false;
            // throw new FolderNameException("Folder name is incorrect");
        }
    }
}
