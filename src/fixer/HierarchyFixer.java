package fixer;

import java.io.File;
import java.util.ArrayList;

public class HierarchyFixer {

    // Moves question folders found at the root into the username folder.
    // Returns false if the username folder cannot be created.
    public static boolean fix(File root, File usernameFolder,
            String[] questions, ArrayList<String> messages) {
        boolean questionsAtRoot = false;

        for (String question : questions) {
            if (new File(root, question).isDirectory()) {
                questionsAtRoot = true;
                break;
            }
        }

        if (!questionsAtRoot) {
            return true;
        }

        if (usernameFolder.exists() && !usernameFolder.isDirectory()) {
            messages.add("Cannot create username folder: "
                    + "a file has that name.");
            return false;
        }

        usernameFolder.mkdirs();

        for (String question : questions) {
            File source = new File(root, question);
            File destination = new File(usernameFolder, question);

            if (!source.isDirectory()) {
                continue;
            }

            if (destination.exists()) {
                messages.add("Cannot move " + question
                        + ": destination already exists.");
            } else if (source.renameTo(destination)) {
                messages.add("Moved " + question
                        + " into the username folder.");
            } else {
                messages.add("Cannot move " + question + ".");
            }
        }

        return true;
    }
}
