import java.util.ArrayList;

import fileClasses.FilesClass;
import fileClasses.Folder;
import fileHandling.*;

public class main {
    public static void main(String[] args) {
        fileHandlings fileHandling = new fileHandlings();
        try {
            fileHandling.unzip("../student-submission/2023-2024-fen.lai.2022.zip", "unzippedFolder");
            ArrayList<FilesClass> arrayOfFiles = fileHandling.viewFilesAndFolders("unzippedFolder");
            loopFile(arrayOfFiles);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loopFile(ArrayList<FilesClass> arrayOfFiles) {
        
        for (int i = 0; i < arrayOfFiles.size(); i++) {
            System.out.println("Parent folder: "+arrayOfFiles.get(i).getParentName());
            System.out.println(arrayOfFiles.get(i).getName() + " " + arrayOfFiles.get(i).getClass());
            if (arrayOfFiles.get(i) instanceof Folder) {
                Folder file = (Folder) arrayOfFiles.get(i);
                ArrayList<FilesClass> itemsInsideFile = file.retrieveFilesInsideFolder();
                loopFile(itemsInsideFile);
            }
        }
    }
}
