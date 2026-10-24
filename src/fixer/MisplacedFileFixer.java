package fixer;

import java.io.File;
import java.util.ArrayList;

public class MisplacedFileFixer {
    private static final int FOLDER_INDEX = 0; // see expectedFiles below

    public static void fix(File usernameFolder, ArrayList<String> messages) {
        File q1Folder = new File(usernameFolder, "Q1");
        q1Folder.mkdirs();

        String[] q1Items = { "Q1a", "Q1b", "Q1a.java", "Q1b.java" };

        for (String itemName : q1Items) {
            File source = new File(usernameFolder, itemName);
            File destination = new File(q1Folder, itemName);

            if (!source.exists()) {
                continue;
            }

            if (destination.exists()) {
                messages.add("Cannot move " + itemName
                        + ": it already exists in Q1.");
            } else if (source.renameTo(destination)) {
                messages.add("Moved " + itemName + " into Q1.");
            } else {
                messages.add("Cannot move " + itemName + ".");
            }
        }

        // First item is the folder, the rest are the files that belong in it
        String[][] expectedFiles = {
                { "Q2", "DataException.class", "persons.txt", "students.txt",
                        "personstester.txt", "studentstester.txt" },
                { "Q3", "Circle.class", "Rectangle.class", "Shape.class",
                        "ShapeComparator.java" }
        };

        for (String[] group : expectedFiles) {
            String folderName = group[FOLDER_INDEX];
            File correctFolder = new File(usernameFolder, folderName);

            for (int i = FOLDER_INDEX + 1; i < group.length; i++) {
                String fileName = group[i];
                File destination = new File(correctFolder, fileName);

                // Create the expected question folder if necessary.
                correctFolder.mkdirs();

                // Search within the username folder for this exact filename.
                File source = findFile(usernameFolder, fileName, destination);

                if (source == null) {
                    continue;
                }

                if (destination.exists()) {
                    messages.add("Cannot move " + fileName
                            + ": destination already exists.");
                } else if (source.renameTo(destination)) {
                    messages.add("Moved " + fileName + " to " + folderName
                            + ".");
                } else {
                    messages.add("Cannot move " + fileName + ".");
                }
            }
        }
    }

    // Looks through the folder and its subfolders for a file with this name,
    // skipping the file if it is already in the right place
    private static File findFile(File folder, String fileName,
            File destination) {
        File[] items = folder.listFiles();

        if (items == null) {
            return null;
        }

        for (File item : items) {
            boolean isInRightPlace = item.getAbsolutePath()
                    .equals(destination.getAbsolutePath());

            if (item.isFile() && item.getName().equalsIgnoreCase(fileName)
                    && !isInRightPlace) {
                return item;
            }

            if (item.isDirectory()) {
                File found = findFile(item, fileName, destination);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}
