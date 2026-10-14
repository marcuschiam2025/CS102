import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import ThrowException.MissingNameOrEmail;
import fileClasses.FilesClass;
import fileClasses.Folder;
import fileHandling.*;

public class main {
    public static void main(String[] args) {
        MissingNameOrEmail.catchMissingNames();
    }

    // public static void loopFile(ArrayList<FilesClass> arrayOfFiles) {

    // for (int i = 0; i < arrayOfFiles.size(); i++) {
    // System.out.println("Parent folder: "+arrayOfFiles.get(i).getParentName());
    // System.out.println(arrayOfFiles.get(i).getName() + " " +
    // arrayOfFiles.get(i).getClass());
    // if (arrayOfFiles.get(i) instanceof Folder) {
    // Folder file = (Folder) arrayOfFiles.get(i);
    // ArrayList<FilesClass> itemsInsideFile = file.retrieveFilesInsideFolder();
    // loopFile(itemsInsideFile);
    // }
    // }
    // }
}
