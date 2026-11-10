package fixer;

import exceptions.AdditionalFileException;
import exceptions.FolderNameException;
import exceptions.MissingEmailException;
import exceptions.MissingFileException;
import exceptions.MissingFolderException;
import exceptions.MissingNameException;
import java.io.File;
import java.util.ArrayList;

// Repairs a submission based on the problems FileStructureChecker found.
// Only the fixers that match those problems are run:
//
//   AdditionalFileException                  -> JunkFileCleaner
//   (always)                                 -> NestedFolderFixer
//   FolderNameException, MissingFolderException -> HierarchyFixer,
//                                               then FolderNameFixer
//   MissingFileException, MissingFolderException -> MisplacedFileFixer
//   MissingNameException, MissingEmailException  -> HeaderFixer
//
// Returns a message for every fix. Anything that could not be done is
// added as "Could not fix: ...". The checker is run again afterwards
// (in GradingCtrl) to report what is still wrong.
public class SubmissionFixer {
    private static final String[] QUESTIONS = { "Q1", "Q2", "Q3" };

    public static ArrayList<String> repairSubmission(String zipFilePath,
            String extractedDirectory, String modelFolder,
            ArrayList<Exception> foundProblems) {
        ArrayList<String> messages = new ArrayList<>();

        String username = UsernameResolver.getUsername(zipFilePath);

        if (username == null) {
            messages.add("Cannot determine username from ZIP filename.");
            return messages;
        }

        String studentName = UsernameResolver.getStudentName(username);
        File root = new File(extractedDirectory).getAbsoluteFile();
        File usernameFolder = new File(root, username);

        // Problems that happen while fixing, e.g. a rename that fails
        ArrayList<Exception> fixProblems = new ArrayList<>();

        // 1. Remove junk first, so a __MACOSX folder is not mistaken
        //    for the student's folder
        if (hasProblem(foundProblems, AdditionalFileException.class)) {
            JunkFileCleaner.junkCleaner(root, messages, fixProblems);
        }

        // 2. Pull Q1/Q2/Q3 out of any extra folders, e.g.
        //    MyExam/<username>/Q1 or <username>/<username>/Q1. Always run:
        //    it checks the layout itself, and the grader needs
        //    <username>/Q1 exactly.
        NestedFolderFixer.fix(root, usernameFolder, messages, fixProblems);

        // 3. Get the username folder right. HierarchyFixer goes first so
        //    that Q1/Q2/Q3 at the top are moved, not renamed.
        if (hasProblem(foundProblems, FolderNameException.class)
                || hasProblem(foundProblems, MissingFolderException.class)) {
            HierarchyFixer.hierarchyFix(root, usernameFolder, QUESTIONS, messages,
                    fixProblems);
            FolderNameFixer.folderNameFix(root, username, messages, fixProblems);
        }

        if (usernameFolder.isDirectory()) {
            // 4. Move files into the right question folder
            if (hasProblem(foundProblems, MissingFileException.class)
                    || hasProblem(foundProblems,
                            MissingFolderException.class)) {
                MisplacedFileFixer.misplacedFileFix(usernameFolder, messages,
                        fixProblems);
            }

            // 5. Fix the name/email comment in the .java files
            if (hasProblem(foundProblems, MissingNameException.class)
                    || hasProblem(foundProblems,
                            MissingEmailException.class)) {
                HeaderFixer.headerFix(usernameFolder, username, studentName,
                        messages, fixProblems);
            }
        }

        // Compare after the layout has been repaired, so nested question
        // folders are moved into place before model-only cleanup.
        if (usernameFolder.isDirectory()) {
            JunkFileCleaner.extraFileCleaner(usernameFolder,
                    new File(modelFolder), messages);
        }

        for (Exception problem : fixProblems) {
            messages.add("Could not fix: " + problem.getMessage());
        }

        return messages;
    }

    // True if any of the problems is of this exception type
    private static boolean hasProblem(ArrayList<Exception> problems,
            Class<? extends Exception> type) {
        for (Exception problem : problems) {
            if (type.isInstance(problem)) {
                return true;
            }
        }

        return false;
    }
}
