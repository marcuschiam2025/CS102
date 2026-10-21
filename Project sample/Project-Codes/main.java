import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

import ThrowException.MissingNameOrEmail;
import fileClasses.FilesClass;
import fileClasses.Folder;
import fileHandling.*;

public class main {
    public static void main(String[] args) {
        // fileHandlings fileHandling = new fileHandlings();
        String filePath = "../student-submission";
        try {
            ArrayList<FilesClass> itemsInside = fileHandlings.viewFilesAndFolders("../student-submission");
            itemsInside.forEach(file -> {
                try {
                    fileHandlings.unzip(filePath + "/" + file.getName(), "unzippedFolder/" + file.getName());

                    ArrayList<Exception> arrayListOfErrors= MissingNameOrEmail.catchMissingNames("unzippedFolder/" + file.getName());
                    // Path pathToDelete = Paths.get("unzippedFolder");
                    // deleteFolder(pathToDelete);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }

    }

    public static void deleteFolder(Path folder) throws IOException {
        if (Files.exists(folder)) {
            try (var paths = Files.walk(folder)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }
    // public static void loopFile(ArrayList<FilesClass> arrayOfFiles, String
    // filePath) {

    // for (int i = 0; i < arrayOfFiles.size(); i++) {
    // System.out.println("Parent folder: " + arrayOfFiles.get(i).getParentName());
    // System.out.println(arrayOfFiles.get(i).getName() + " " +
    // arrayOfFiles.get(i).getClass());

    // if (arrayOfFiles.get(i) instanceof Folder) {
    // Folder file = (Folder) arrayOfFiles.get(i);
    // ArrayList<FilesClass> itemsInsideFile = file.retrieveFilesInsideFolder();
    // loopFile(itemsInsideFile, filePath);
    // }
    // }
    // }
}
