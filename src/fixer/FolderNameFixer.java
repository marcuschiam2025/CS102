package fixer;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class FolderNameFixer {

    // Renames the first top-level folder that is not named after the username
    public static void fix(File root, String username,
            ArrayList<String> messages) {
        ArrayList<Exception> problems = new ArrayList<>();
        fix(root, username, messages, problems);
        addProblemMessages(messages, problems);
    }

    public static void fix(File root, String username,
            ArrayList<String> messages, ArrayList<Exception> problems) {
        File usernameFolder = new File(root, username);
        File[] items = root.listFiles();

        if (items == null) {
            if (root.isDirectory()) {
                problems.add(new IOException("Cannot inspect folder "
                        + root + "."));
            }
            return;
        }

        for (File folder : items) {
            if (!folder.isDirectory() || folder.getName().equals(username)) {
                continue;
            }

            if (usernameFolder.exists()) {
                problems.add(new IOException("Cannot rename folder " + folder.getName()
                        + ": destination folder " + username
                        + " already exists."));
                continue;
            }

            if (folder.renameTo(usernameFolder)) {
                messages.add("Renamed folder " + folder.getName()
                        + " to " + username + ".");
            } else {
                problems.add(new IOException("Cannot rename folder "
                        + folder.getName() + "."));
            }
            break;
        }
    }

    private static void addProblemMessages(ArrayList<String> messages,
            ArrayList<Exception> problems) {
        for (Exception problem : problems) {
            messages.add(problem.getMessage());
        }
    }
}
