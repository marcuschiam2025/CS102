package fixer;

import filehandling.fileHandlings;
import java.io.File;
import java.util.ArrayList;

public class JunkFileCleaner {
    private static final String MAC_FOLDER = "__MACOSX"; //edge case
    private static final String MAC_FILE = ".DS_Store";
    private static final String CLASS_EXTENSION = ".class";
    private static final String JAVA_EXTENSION = ".java";
    private static final String INNER_CLASS_SEPARATOR = "$";
    private static final int NOT_FOUND = -1;

    // Removes files that are not part of the submission:
    // - __MACOSX folders and .DS_Store files that macOS adds to zips
    // - old .class files of the student's own code,
    //   e.g. Q1a.class next to Q1a.java.
    //   Class files given with the exam (e.g. Circle.class, which has
    //   no Circle.java) are kept.
    public static void clean(File folder, ArrayList<String> messages) {
        File[] items = folder.listFiles();

        if (items == null) {
            return;
        }

        for (File item : items) {
            String name = item.getName();

            if (item.isDirectory()) {
                if (name.equals(MAC_FOLDER)) {
                    fileHandlings.deleteFolder(item);
                    messages.add("Removed " + MAC_FOLDER + " folder.");
                } else {
                    clean(item, messages);
                }
            } else if (name.equals(MAC_FILE)) {
                if (item.delete()) {
                    messages.add("Removed " + MAC_FILE + " from "
                            + folder.getName() + ".");
                }
            } else if (name.endsWith(CLASS_EXTENSION)
                    && hasJavaFile(folder, name)) {
                if (item.delete()) {
                    messages.add("Removed old compiled file " + name
                            + " from " + folder.getName() + ".");
                }
            }
        }
    }

    // Checks if the .java file that this .class file was compiled from
    // is in the same folder.
    // e.g. Q1a.class -> Q1a.java, and inner classes like
    // Q2a$Helper.class -> Q2a.java
    private static boolean hasJavaFile(File folder, String classFileName) {
        int nameLength = classFileName.length() - CLASS_EXTENSION.length();
        String className = classFileName.substring(0, nameLength);

        int separatorPosition = className.indexOf(INNER_CLASS_SEPARATOR);
        if (separatorPosition != NOT_FOUND) {
            className = className.substring(0, separatorPosition);
        }

        return new File(folder, className + JAVA_EXTENSION).isFile();
    }
}
