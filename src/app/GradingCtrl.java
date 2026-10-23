package app;

import filehandling.fileHandlings;
import fixer.SubmissionFixer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

// Does the work behind the menu and keeps the settings. It never prints anything,
// it only returns results so the menu can decide how to show them.
public class GradingCtrl {
    public static final int MIN_TIME_LIMIT = 1;
    public static final int MAX_TIME_LIMIT = 60;

    private static final String UNZIP_FOLDER = "unzippedFolder";

    private String submissionsFolder = "Project sample/student-submission";
    private String testerFolder = "Project sample/Tester-Files";
    private String scoreSheetFile = "Project sample/IS442-ScoreSheet.csv";
    private int timeLimitSeconds = 5;

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

    // Returns false (and keeps the old folder) if the input is empty or the folder does not exist
    public boolean setSubmissionsFolder(String folder) {
        if (folder.isEmpty() || !new File(folder).isDirectory()) {
            return false;
        }
        submissionsFolder = folder;
        return true;
    }

    // Returns false (and keeps the old folder) if the input is empty or the folder does not exist
    public boolean setTesterFolder(String folder) {
        if (folder.isEmpty() || !new File(folder).isDirectory()) {
            return false;
        }
        testerFolder = folder;
        return true;
    }

    // Returns false (and keeps the old file) if it is not an existing .csv file
    public boolean setScoreSheetFile(String file) {
        if (file.isEmpty() || !file.toLowerCase().endsWith(".csv") || !new File(file).isFile()) {
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

    // Returns the zip files sorted by name, an empty array if there are none,
    // or null if the submissions folder does not exist
    public File[] findZipFiles() {
        File folder = new File(submissionsFolder);

        if (!folder.isDirectory()) {
            return null;
        }

        ArrayList<File> zipList = new ArrayList<>();

        for (File file : folder.listFiles()) {
            if (file.getName().toLowerCase().endsWith(".zip")) {
                zipList.add(file);
            }
        }

        File[] zipFiles = zipList.toArray(new File[0]);
        Arrays.sort(zipFiles);
        return zipFiles;
    }

    // Unzips and fixes one submission. Returns the list of fixes made.
    public ArrayList<String> processSubmission(File zipFile) throws IOException {
        String zipName = zipFile.getName();
        zipName = zipName.substring(0, zipName.length() - 4); // remove ".zip"
        File extractFolder = new File(UNZIP_FOLDER, zipName);

        // 1. Unzip into a fresh folder for this student
        fileHandlings.deleteFolder(extractFolder);
        fileHandlings.unzip(zipFile.getPath(), extractFolder.getPath());

        // 2. Validate (Shanna, ShiYing): add here once their branch is merged

        // 3. Fix
        ArrayList<String> fixes = SubmissionFixer.repairSubmission(
                zipFile.getPath(), extractFolder.getPath());

        // 4. Grade (Shoban, Cedric): add here once grading is merged,
        //    using testerFolder and timeLimitSeconds

        return fixes;
    }
}
