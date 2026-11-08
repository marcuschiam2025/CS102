package app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import model.StudentResult;

// Shows the menus and reads what the user types.
// The actual work is done by GradingCtrl.
public class GradingMenu {
    private static final int NO_MORE_INPUT = -1;
    private static final int GO_BACK = 0;

    // Main menu options, in the order the work is done
    private static final int CHECK = 1;
    private static final int FIX = 2;
    private static final int GRADE = 3;
    private static final int VIEW_RESULTS = 4;
    private static final int EXPORT_CSV = 5;
    private static final int RUN_ALL = 6;
    private static final int RUN_ALL_ONE = 7;
    private static final int SETTINGS = 8;
    private static final int EXIT = 9;

    // Settings menu options
    private static final int SET_SUBMISSIONS_FOLDER = 1;
    private static final int SET_TESTER_FOLDER = 2;
    private static final int SET_SCORE_SHEET = 3;
    private static final int SET_TIME_LIMIT = 4;
    private static final int BACK = 5;

    private Scanner sc;
    private GradingCtrl ctrl;

    public GradingMenu() {
        sc = new Scanner(System.in);
        ctrl = new GradingCtrl();
    }

    public void run() {
        int choice = 0;

        while (choice != EXIT && choice != NO_MORE_INPUT) {
            displayMainMenu();
            choice = readNumber("Enter your choice: ", CHECK, EXIT);

            if (choice == CHECK) {
                processAllSubmissions(ctrl::checkSubmission);
            } else if (choice == FIX) {
                processAllSubmissions(ctrl::fixSubmission);
            } else if (choice == GRADE) {
                gradeAllSubmissions();
            } else if (choice == VIEW_RESULTS) {
                viewResults();
            } else if (choice == EXPORT_CSV) {
                exportResults();
            } else if (choice == RUN_ALL) {
                runAllStepsOnAll();
            } else if (choice == RUN_ALL_ONE) {
                runAllStepsOnOne();
            } else if (choice == SETTINGS) {
                settingsMenu();
            }
        }

        System.out.println("Goodbye!");
        sc.close();
    }

    private void displayMainMenu() {
        System.out.println("\n========== Auto Grading System ==========");
        System.out.println("-- Grading steps --");
        System.out.println(CHECK + ". Check submissions   (find problems)");
        System.out.println(FIX + ". Fix submissions     (repair what was "
                + "found)");
        System.out.println(GRADE + ". Grade submissions   (run the testers)");
        System.out.println("-- Results --");
        System.out.println(VIEW_RESULTS + ". View results");
        System.out.println(EXPORT_CSV + ". Export results to CSV");
        System.out.println("-- Other --");
        System.out.println(RUN_ALL + ". Run all steps (" + CHECK + " -> "
                + FIX + " -> " + GRADE + ") on all submissions");
        System.out.println(RUN_ALL_ONE + ". Run all steps on one submission");
        System.out.println(SETTINGS + ". Settings");
        System.out.println(EXIT + ". Exit");
    }

    // One grading step run on one submission, e.g. ctrl::checkSubmission.
    // Returns the lines to show the user.
    private interface Step {
        ArrayList<String> run(File zipFile) throws IOException;
    }

    // ---------- Reading input ----------

    // Keeps asking until the user enters a whole number from min to max.
    // Returns NO_MORE_INPUT if the input has ended,
    // so the program can never get stuck.
    private int readNumber(String prompt, int min, int max) {
        System.out.print(prompt);

        while (sc.hasNextLine()) {
            String input = sc.nextLine().trim();

            if (isNumberInRange(input, min, max)) {
                return Integer.parseInt(input);
            }

            System.out.println("Invalid input. Please enter a number from "
                    + min + " to " + max + ".");
            System.out.print(prompt);
        }

        return NO_MORE_INPUT;
    }

    private boolean isNumberInRange(String input, int min, int max) {
        try {
            int number = Integer.parseInt(input);
            return number >= min && number <= max;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    // Returns what the user typed, or an empty String to cancel
    private String readText(String prompt) {
        System.out.print(prompt);

        if (!sc.hasNextLine()) {
            return "";
        }

        return sc.nextLine().trim();
    }

    // ---------- Main menu actions ----------

    private void gradeAllSubmissions() {
        StudentResult.loadQuestionResultTemplate();
        processAllSubmissions(ctrl::gradeSubmission);
    }

    private void runAllStepsOnAll() {
        StudentResult.loadQuestionResultTemplate();
        processAllSubmissions(ctrl::processSubmission);
    }

    private void runAllStepsOnOne() {
        File zipFile = chooseSubmission();

        if (zipFile == null) {
            return;
        }

        StudentResult.loadQuestionResultTemplate();
        System.out.println("\n" + zipFile.getName());

        if (processSubmission(zipFile, ctrl::processSubmission)) {
            System.out.println("\nDone.");
        }
    }

    private void viewResults() {
        // Shoban, Cedric: show each student's scores here once grading
        // is merged
        ctrl.viewResults();
        // System.out.println("\nNot available yet: results will be shown "
        //         + "here once grading is added.");
    }

    private void exportResults() {
        if (!ctrl.hasResults()) {
            System.out.println("\nNothing to export yet. Grade the "
                    + "submissions first.");
        } else if (ctrl.exportCSV()) {
            System.out.println("\nSuccessfully exported to "
                    + ctrl.getExportFile());
        } else {
            System.out.println("\nExport failed: could not write "
                    + ctrl.getExportFile() + ". Close it if it is open in "
                    + "another program (e.g. Excel) and try again.");
        }
    }

    // Runs one step (or all of them) on every submission
    private void processAllSubmissions(Step step) {
        File[] zipFiles = getZipFiles();

        if (zipFiles == null) {
            return;
        }

        int failed = 0;

        for (int i = 0; i < zipFiles.length; i++) {
            System.out.println("\n[" + (i + 1) + "/" + zipFiles.length
                    + "] " + zipFiles[i].getName());

            if (!processSubmission(zipFiles[i], step)) {
                failed++;
            }
        }

        int processed = zipFiles.length - failed;
        System.out.println("\nDone. " + processed + " of " + zipFiles.length
                + " submissions processed.");
    }

    // Returns true if the submission was processed, false if it failed.
    // One submission failing must not stop the others.
    private boolean processSubmission(File zipFile, Step step) {
        try {
            ArrayList<String> fixes = step.run(zipFile);

            for (String fix : fixes) {
                System.out.println("  - " + fix);
            }
            return true;
        } catch (Exception exception) {
            System.out.println("  Failed: " + exception.getMessage());
            return false;
        }
    }

    // Lists the submissions and lets the user pick one.
    // Returns null if the user goes back.
    private File chooseSubmission() {
        File[] zipFiles = getZipFiles();

        if (zipFiles == null) {
            return null;
        }

        System.out.println("\nSubmissions:");
        for (int i = 0; i < zipFiles.length; i++) {
            System.out.println((i + 1) + ". " + zipFiles[i].getName());
        }

        int number = readNumber("Enter a submission number (0 to go back): ",
                GO_BACK, zipFiles.length);

        if (number == GO_BACK || number == NO_MORE_INPUT) {
            return null;
        }

        return zipFiles[number - 1];
    }

    // Returns the zip files, or null (after telling the user why)
    // if there are none
    private File[] getZipFiles() {
        File[] zipFiles = ctrl.findZipFiles();

        if (zipFiles == null) {
            System.out.println("Submissions folder not found: "
                    + ctrl.getSubmissionsFolder());
            return null;
        }

        if (zipFiles.length == 0) {
            System.out.println("No ZIP files found in "
                    + ctrl.getSubmissionsFolder());
            return null;
        }

        return zipFiles;
    }

    // ---------- Settings ----------

    private void settingsMenu() {
        int choice = 0;

        while (choice != BACK && choice != NO_MORE_INPUT) {
            displaySettingsMenu();
            choice = readNumber("Enter your choice: ",
                    SET_SUBMISSIONS_FOLDER, BACK);

            if (choice == SET_SUBMISSIONS_FOLDER) {
                String folder = readText("Enter the new submissions folder "
                        + "(leave empty to cancel): ");
                showResult(folder, ctrl.setSubmissionsFolder(folder),
                        "Folder not found");
            } else if (choice == SET_TESTER_FOLDER) {
                String folder = readText("Enter the new tester files folder "
                        + "(leave empty to cancel): ");
                showResult(folder, ctrl.setTesterFolder(folder),
                        "Folder not found");
            } else if (choice == SET_SCORE_SHEET) {
                String file = readText("Enter the new score sheet file "
                        + "(leave empty to cancel): ");
                showResult(file, ctrl.setScoreSheetFile(file),
                        "Not an existing .csv file");
            } else if (choice == SET_TIME_LIMIT) {
                changeTimeLimit();
            }
        }
    }

    private void displaySettingsMenu() {
        System.out.println("\n---------- Settings ----------");
        System.out.println(SET_SUBMISSIONS_FOLDER + ". Submissions folder: "
                + ctrl.getSubmissionsFolder());
        System.out.println(SET_TESTER_FOLDER + ". Tester files folder: "
                + ctrl.getTesterFolder());
        System.out.println(SET_SCORE_SHEET + ". Score sheet (CSV): "
                + ctrl.getScoreSheetFile());
        System.out.println(SET_TIME_LIMIT + ". Time limit per program: "
                + ctrl.getTimeLimitSeconds() + " seconds");
        System.out.println(BACK + ". Back to main menu");
    }

    private void changeTimeLimit() {
        int min = GradingCtrl.MIN_TIME_LIMIT;
        int max = GradingCtrl.MAX_TIME_LIMIT;
        int seconds = readNumber("Enter the time limit in seconds (" + min
                + "-" + max + "): ", min, max);

        if (seconds != NO_MORE_INPUT && ctrl.setTimeLimitSeconds(seconds)) {
            System.out.println("Time limit changed to " + seconds
                    + " seconds.");
        }
    }

    // Tells the user whether a setting was changed
    private void showResult(String input, boolean changed,
            String errorMessage) {
        if (input.isEmpty()) {
            System.out.println("Not changed.");
        } else if (changed) {
            System.out.println("Changed to: " + input);
        } else {
            System.out.println(errorMessage + ": " + input
                    + ". Not changed.");
        }
    }
}
