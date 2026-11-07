package fixer;

import OwnExceptions.FolderNameException;
import OwnExceptions.MissingFolderException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SubmissionFixer {
    private static final String[] QUESTIONS = { "Q1", "Q2", "Q3" };

    public static ArrayList<String> repairSubmission(String zipFilePath,
            String extractedDirectory) {
        ArrayList<Exception> problems = new ArrayList<>();
        ArrayList<String> messages = repairSubmission(zipFilePath,
                extractedDirectory, problems);
        addProblemMessages(messages, problems);
        return messages;
    }

    public static ArrayList<String> repairSubmission(String zipFilePath,
            String extractedDirectory, ArrayList<Exception> problems) {
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
        if (!root.isDirectory() && !root.mkdirs() && !root.isDirectory()) {
            problems.add(new IOException(
                    "Cannot create extraction folder " + root + "."));
        }

        File usernameFolder = new File(root, username);

        // 0. Remove junk files first, so a __MACOSX folder is not mistaken
        //    for the student's folder.
        JunkFileCleaner.clean(root, messages, problems);

        // 1. Rename a wrongly named top-level folder.
        FolderNameFixer.fix(root, username, messages, problems);

        // 2. Move question folders into the username folder.
        HierarchyFixer.fix(root, usernameFolder, QUESTIONS, messages,
                problems);

        if (usernameFolder.isDirectory()) {
            // 3. Move files that are in the wrong question folder.
            MisplacedFileFixer.fix(usernameFolder, messages, problems);

            // 4. Check Java comments inside Q1, Q2 and Q3.
            HeaderFixer.fix(usernameFolder, username, studentName, messages,
                    problems);
        }

        checkRemainingProblems(usernameFolder, problems);

        if (messages.isEmpty() && problems.isEmpty()) {
            messages.add("No targeted errors found.");
        }

        return messages;
    }

    private static void checkRemainingProblems(File usernameFolder,
            ArrayList<Exception> problems) {
        if (!usernameFolder.isDirectory()) {
            problems.add(new FolderNameException(
                    "The username folder is still missing after repair."));
            return;
        }

        for (String question : QUESTIONS) {
            if (!new File(usernameFolder, question).isDirectory()) {
                problems.add(new MissingFolderException(
                        "Missing question folder after repair: " + question));
            }
        }
    }

    private static void addProblemMessages(ArrayList<String> messages,
            ArrayList<Exception> problems) {
        for (Exception problem : problems) {
            messages.add("Problem [" + problem.getClass().getSimpleName()
                    + "]: " + problem.getMessage());
        }
    }
}
