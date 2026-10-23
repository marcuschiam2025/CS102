package fixer;

import filehandling.fileHandlings;
import java.io.File;
import java.util.ArrayList;
public class JunkFileCleaner {
    private static final String MAC_FOLDER = "__MACOSX"; //edge case
    private static final String MAC_FILE = ".DS_Store";

    // Removes files that are not part of the submission:
    // - __MACOSX folders and .DS_Store files that macOS adds to zips
    // - old .class files of the student's own code, e.g. Q1a.class next to Q1a.java.
    //   Class files given with the exam (e.g. Circle.class, which has no Circle.java) are kept.
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
                    messages.add("Removed " + MAC_FILE + " from " + folder.getName() + ".");
                }
            } else if (name.endsWith(".class") && hasJavaFile(folder, name)) {
                if (item.delete()) {
                    messages.add("Removed old compiled file " + name + " from " + folder.getName() + ".");
                }
            }
        }
    }

    // Checks if the .java file that this .class file was compiled from is in the same folder.
    // e.g. Q1a.class -> Q1a.java, and inner classes like Q2a$Helper.class -> Q2a.java
    private static boolean hasJavaFile(File folder, String classFileName) {
        String className = classFileName.substring(0, classFileName.length() - ".class".length());

        int dollarPosition = className.indexOf("$");
        if (dollarPosition != -1) {
            className = className.substring(0, dollarPosition);
        }

        return new File(folder, className + ".java").isFile();
    }
}
