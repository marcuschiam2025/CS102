package checker;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

// import exceptions.FolderNameException;

public class FolderNameChecker {
    public static boolean isValidFolderName(String filepath){
        Path path = Paths.get(filepath);

        // Get the folder's name directly
        String folderName = path.getFileName().toString();

        if(Pattern.matches("^[A-Za-z]+[.][0-9]{4}$", folderName) || Pattern.matches("^[A-Za-z]+[.][A-Za-z]+[.][0-9]{4}$", folderName)){
            return  true;
        }else{
            return  false;
            // throw new FolderNameException("Folder name is incorrect");
        }
    }
}
