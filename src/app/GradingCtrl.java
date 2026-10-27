package app;

import filehandling.fileHandlings;
import fixer.SubmissionFixer;
import java.io.File;
import java.io.IOException;
import fixer.UsernameResolver;
import java.util.ArrayList;
import java.util.Arrays;
import grader.Grader;

// Does the work behind the menu and keeps the settings. It never prints
// anything, it only returns results so the menu can decide how to show them.
public class GradingCtrl {
    public static final int MIN_TIME_LIMIT = 1;
    public static final int MAX_TIME_LIMIT = 60;

    private static final int DEFAULT_TIME_LIMIT = 5;
    private static final String UNZIP_FOLDER = "unzippedFolder";
    private static final String ZIP_EXTENSION = ".zip";
    private static final String CSV_EXTENSION = ".csv";

    private String submissionsFolder = "Project sample/student-submission";
    private String testerFolder = "Project sample/TesterFiles";
    private String scoreSheetFile = "Project sample/IS442-ScoreSheet.csv";
    private int timeLimitSeconds = DEFAULT_TIME_LIMIT;

    public String getSubmissionsFolder() {
        return submissionsFolder;
    }

    public String getTesterFolder() {
        return testerFolder;
    }

    public String getScoreSheetFile() {
        return scoreSheetFile;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    // Returns false (and keeps the old folder) if the input is empty
    // or the folder does not exist
    public boolean setSubmissionsFolder(String folder) {
        if (!isExistingFolder(folder)) {
            return false;
        }

        submissionsFolder = folder;
        return true;
    }

    // Returns false (and keeps the old folder) if the input is empty
    // or the folder does not exist
    public boolean setTesterFolder(String folder) {
        if (!isExistingFolder(folder)) {
            return false;
        }

        testerFolder = folder;
        return true;
    }

    // Returns false (and keeps the old file) if it is not an existing
    // .csv file
    public boolean setScoreSheetFile(String file) {
        boolean isCsvFile = file.toLowerCase().endsWith(CSV_EXTENSION);

        if (file.isEmpty() || !isCsvFile || !new File(file).isFile()) {
            return false;
        }

        scoreSheetFile = file;
        return true;
    }

    // Returns false (and keeps the old time limit) if it is out of range
    public boolean setTimeLimitSeconds(int seconds) {
        if (seconds < MIN_TIME_LIMIT || seconds > MAX_TIME_LIMIT) {
            return false;
        }

        timeLimitSeconds = seconds;
        return true;
    }

    private boolean isExistingFolder(String folder) {
        return !folder.isEmpty() && new File(folder).isDirectory();
    }

    // Returns the zip files sorted by name, an empty array if there are
    // none, or null if the submissions folder does not exist
    public File[] findZipFiles() {
        File folder = new File(submissionsFolder);

        if (!folder.isDirectory()) {
            return null;
        }

        ArrayList<File> zipList = new ArrayList<>();

        for (File file : folder.listFiles()) {
            if (file.getName().toLowerCase().endsWith(ZIP_EXTENSION)) {
                zipList.add(file);
            }
        }

        File[] zipFiles = zipList.toArray(new File[0]);
        Arrays.sort(zipFiles);
        return zipFiles;
    }

    // Unzips and fixes one submission. Returns the list of fixes made.
    public ArrayList<String> processSubmission(File zipFile)
            throws IOException {
        String zipName = zipFile.getName();
        int nameLength = zipName.length() - ZIP_EXTENSION.length();
        File extractFolder = new File(UNZIP_FOLDER,
                zipName.substring(0, nameLength));

        // 1. Unzip into a fresh folder for this student
        fileHandlings.deleteFolder(extractFolder);
        fileHandlings.unzip(zipFile.getPath(), extractFolder.getPath());

        // 2. Validate (Shanna, ShiYing): add here once their branch
        // is merged

        // 3. Fix
        ArrayList<String> fixes = SubmissionFixer.repairSubmission(
                zipFile.getPath(), extractFolder.getPath());

        // 4. Grade (Shoban, Cedric): add here once grading is merged,
        // using testerFolder and timeLimitSeconds

        String username = UsernameResolver.getUsername(zipFile.getPath());

        if (username != null && new File(extractFolder, username).isDirectory()) {
            File usernameFolder = new File(extractFolder, username);
            // grade checker: grade Q1a, Q1b, Q2a, Q2b, Q3
            Grader.gradeSubmission(usernameFolder, new File(testerFolder),
                    timeLimitSeconds);
        } else {
            // can't grade: every question gets 0
        }

        return fixes;
    }
}
