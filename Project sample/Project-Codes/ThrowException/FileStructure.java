package ThrowException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import OwnExceptions.AdditionalFileException;
import OwnExceptions.FolderNameException;
import OwnExceptions.MissingFileException;
import Validations.FolderNameChecker;
import fileHandling.fileHandlings;

public class FileStructure {
    public static ArrayList<Exception> wrongFileStructure(String filePath) {
        ArrayList<Exception> arrayOfExceptions = new ArrayList<>();

        fileHandlings fileHandling = new fileHandlings();

        try {
            ArrayList<String> stringsOfRows = fileHandling.readFiles("\n", "fileStructure/correctFileStructure.txt");

            Path start = Paths.get(filePath);
            Files.walk(start)
                    .forEach(path -> {
                        String pathString = path.toString();
                        String[] folders = pathString.split("/");
                        int sizeOfSubmimttedFolder = folders.length - 2;
                        stringsOfRows.forEach(valuesOfPath -> {
                            String[] correctPathSplitToFilesAndFolders = valuesOfPath.split("/");
                            if (correctPathSplitToFilesAndFolders[correctPathSplitToFilesAndFolders.length - 1]
                                    .equals(folders[folders.length - 1])) {
                                if (correctPathSplitToFilesAndFolders.length < sizeOfSubmimttedFolder) {
                                    for (int i = 2; i < folders.length; i++) {
                                        boolean isInside = checkWhetherItsInside(folders[i],
                                                correctPathSplitToFilesAndFolders);
                                        if (isInside == false) {
                                            System.out.println("Additional file/folder, " + folders[i] + " is created");
                                            arrayOfExceptions.add(new AdditionalFileException(
                                                    "Additional file/folder, " + folders[i] + " is created"));
                                        }
                                    }
                                } else if (correctPathSplitToFilesAndFolders.length > sizeOfSubmimttedFolder) {
                                    for (int i = 0; i < correctPathSplitToFilesAndFolders.length; i++) {
                                        boolean isInside = checkWhetherItsInside(correctPathSplitToFilesAndFolders[i],
                                                folders);
                                        if (isInside == false) {
                                            arrayOfExceptions.add(new MissingFileException(
                                                    "File/folder, " + correctPathSplitToFilesAndFolders[i]
                                                            + " is missing from " + folders[1] ));
                                            System.out.println("File/folder, " + correctPathSplitToFilesAndFolders[i]
                                                    + " is missing from " + folders[1] );
                                        }
                                    }
                                } else {
                                    for (int i = 2; i < folders.length; i++) {
                                        if (i == 2) {
                                            if (!FolderNameChecker.FolderName(folders[i])) {
                                                System.out.println("Folder "+folders[i]+" have to be renamed to username " + filePath);
                                                 arrayOfExceptions.add(new FolderNameException("Folder have to be renamed to username " + folders[i]));
                                            }
                                        } else {
                                            boolean isInside = checkWhetherItsInside(folders[i],
                                                    correctPathSplitToFilesAndFolders);
                                            if (isInside == false) {
                                                arrayOfExceptions
                                                        .add(new FolderNameException("File/folder, " + folders[i]
                                                                + " is wrong and need to be renamed to "
                                                                + correctPathSplitToFilesAndFolders[i - 2]));
                                                System.out.println("File/folder, " + folders[i]
                                                        + " is wrong and need to be renamed to "
                                                        + correctPathSplitToFilesAndFolders[i - 2]);
                                            }
                                        }

                                    }
                                }
                            }

                        });

                    });
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return arrayOfExceptions;
    }

    public static Boolean checkWhetherItsInside(String fileName, String[] foldersWithFilesInside) {
        boolean thingToReturn = false;
        for (int i = 0; i < foldersWithFilesInside.length; i++) {
            if (foldersWithFilesInside[i].equals(fileName)) {
                thingToReturn = true;
            }
        }
        return thingToReturn;
    }
}
