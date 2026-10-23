package fixer;

import java.io.File;
import java.util.ArrayList;

public class SubmissionFixer {
    private static final String[] QUESTIONS = { "Q1", "Q2", "Q3" };

    public static ArrayList<String> repairSubmission(String zipFilePath,
            String extractedDirectory) {
        ArrayList<String> messages = new ArrayList<>();

        String username = UsernameResolver.getUsername(zipFilePath);

        if (username == null) {
            messages.add("Cannot determine username from ZIP filename.");
            return messages;
        }

        String studentName = UsernameResolver.getStudentName(username);

        if (studentName.isEmpty()) {
            messages.add("Cannot derive student name from ZIP filename.");
            return messages;
        }

        File root = new File(extractedDirectory).getAbsoluteFile();
        root.mkdirs();

        File usernameFolder = new File(root, username);

        // 0. Remove junk files first, so a __MACOSX folder is not mistaken
        //    for the student's folder.
        JunkFileCleaner.clean(root, messages);

        // 1. Rename a wrongly named top-level folder.
        FolderNameFixer.fix(root, username, messages);

        // 2. Move question folders into the username folder.
        if (!HierarchyFixer.fix(root, usernameFolder, QUESTIONS, messages)) {
            return messages;
        }

        if (!usernameFolder.isDirectory()) {
            messages.add("Username folder not found. "
                    + "Please check the submission.");
            return messages;
        }

        // 3. Move files that are in the wrong question folder.
        MisplacedFileFixer.fix(usernameFolder, messages);

        // 4. Check Java comments inside Q1, Q2 and Q3.
        HeaderFixer.fix(usernameFolder, username, studentName, messages);

        if (messages.isEmpty()) {
            messages.add("No targeted errors found.");
        }

        return messages;
    }
}
