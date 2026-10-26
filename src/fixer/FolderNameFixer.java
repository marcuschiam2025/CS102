package fixer;

import java.io.File;
import java.util.ArrayList;

public class FolderNameFixer {

    // Renames the first top-level folder that is not named after the username
    public static void fix(File root, String username,
            ArrayList<String> messages) {
        File usernameFolder = new File(root, username);
        File[] items = root.listFiles();

        if (items == null) {
            return;
        }

        for (File folder : items) {
            if (!folder.isDirectory() || folder.getName().equals(username)) {
                continue;
            }

            if (usernameFolder.exists()) {
                messages.add("Cannot rename folder " + folder.getName()
                        + ": destination folder " + username
                        + " already exists.");
                continue;
            }

            if (folder.renameTo(usernameFolder)) {
                messages.add("Renamed folder " + folder.getName()
                        + " to " + username + ".");
            } else {
                messages.add("Cannot rename folder " + folder.getName()
                        + ".");
            }
            break;
        }
    }
}
