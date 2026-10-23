package fixer;

import java.io.File;
import java.util.ArrayList;

public class FolderNameFixer {

    // Renames the student's top-level folder to the username,
    // e.g. RenameToYourUsername -> fen.lai.2022.
    // Run this after HierarchyFixer, so Q1, Q2 and Q3 are no longer at the
    // top level. Question folders are skipped anyway, to be safe.
    public static void fix(File root, String username, String[] questions,
            ArrayList<String> messages) {
        File usernameFolder = new File(root, username);

        // Already correct, nothing to rename
        if (usernameFolder.exists()) {
            return;
        }

        ArrayList<File> folders = findFoldersToRename(root, questions);

        if (folders.isEmpty()) {
            return;
        }

        // Do not guess which folder belongs to the student
        if (folders.size() > 1) {
            messages.add("Cannot rename folder: found more than one folder ("
                    + joinNames(folders) + ").");
            return;
        }

        File folder = folders.get(0);

        if (folder.renameTo(usernameFolder)) {
            messages.add("Renamed folder " + folder.getName()
                    + " to " + username + ".");
        } else {
            messages.add("Cannot rename folder " + folder.getName() + ".");
        }
    }

    // Returns the top-level folders that are not question folders
    private static ArrayList<File> findFoldersToRename(File root,
            String[] questions) {
        ArrayList<File> folders = new ArrayList<>();
        File[] items = root.listFiles();

        if (items == null) {
            return folders;
        }

        for (File item : items) {
            if (item.isDirectory()
                    && !isQuestionFolder(item.getName(), questions)) {
                folders.add(item);
            }
        }

        return folders;
    }

    private static boolean isQuestionFolder(String name, String[] questions) {
        for (String question : questions) {
            if (question.equals(name)) {
                return true;
            }
        }
        return false;
    }

    // e.g. "Folder1, Folder2"
    private static String joinNames(ArrayList<File> folders) {
        String names = "";

        for (File folder : folders) {
            if (!names.isEmpty()) {
                names += ", ";
            }
            names += folder.getName();
        }

        return names;
    }
}
