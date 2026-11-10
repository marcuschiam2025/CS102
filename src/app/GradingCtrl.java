package app;

import filehandling.FileHandlings;
import fixer.SubmissionFixer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import fixer.UsernameResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import model.QuestionResult;
import model.Student;
import model.StudentResult;
import grading.Grading;
import grading.GradingResultsViewer;
import plagiarism.ForeignHeader;
import plagiarism.HeaderChecker;
import plagiarism.SharedWrongOutputPair;
import plagiarism.WrongOutputChecker;
import checker.FileStructureChecker;
import exceptions.InvalidZipException;

// Does the work behind the menu and keeps the settings. It never prints
// anything, it only returns results so the menu can decide how to show them.
public class GradingCtrl {
    public static final int MIN_TIME_LIMIT = 1;
    public static final int MAX_TIME_LIMIT = 60;

    private static final int DEFAULT_TIME_LIMIT = 5;
    private static final String UNZIP_FOLDER = "unzippedFolder";
    private static final String TEMPLATE_ZIP = "src/RenameToYourUsername.zip";
    private static final String ZIP_EXTENSION = ".zip";
    private static final String CSV_EXTENSION = ".csv";
    // The blank exam, used to ignore answers from unchanged template code
    private static final String EXAM_TEMPLATE_FOLDER = "Project sample/RenameToYourUsername";
    // Results are exported here, so the input score sheet is not overwritten
    private static final String EXPORT_FILE = "output/ScoreSheet-graded.csv";

    private static final String NO_USERNAME = "Cannot work out the username "
            + "from the ZIP name, so it was skipped.";
    private static final String NOT_CHECKED = "Skipped: not checked yet. "
            + "Check the submissions first.";
    private static final String NOT_FIXED = "Skipped: not fixed yet. "
            + "Fix the submissions first.";

    private String submissionsFolder = "src/student-submission";
    private String testerFolder = "Project sample/TesterFiles";
    private String scoreSheetFile = "Project sample/IS442-ScoreSheet.csv";
    private String studentResultTemplateFile =
            "Project sample/StudentResultTemplate.csv";
    private static String modelFolder = "Project sample/RenameToYourUsername";
    private int timeLimitSeconds = DEFAULT_TIME_LIMIT;

    private ArrayList<StudentResult> studentResults = new ArrayList<>();

    // What each step has done so far, by ZIP file path:
    // problems found by the last check (or left after fixing),
    // and the submissions that went through the fix step
    private HashMap<String, ArrayList<Exception>> foundProblems =
            new HashMap<>();
    private HashSet<String> fixedSubmissions = new HashSet<>();

    public String getSubmissionsFolder() {
        return submissionsFolder;
    }

    public String getTesterFolder() {
        return testerFolder;
    }

    public String getScoreSheetFile() {
        return scoreSheetFile;
    }

    public String getStudentResultTemplateFile() {
        return studentResultTemplateFile;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public static String getModelFolder() {
        return modelFolder;
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

    // Returns false (and keeps the old file) if it is not an existing
    // .csv file
    public boolean setStudentResultTemplateFile(String file) {
        boolean isCsvFile = file.toLowerCase().endsWith(CSV_EXTENSION);

        if (file.isEmpty() || !isCsvFile || !new File(file).isFile()) {
            return false;
        }

        studentResultTemplateFile = file;
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

    public boolean setModelFolder(String folder) {
        if (!isExistingFolder(folder)) {
            return false;
        }

        modelFolder = folder;
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

    // Runs every step on one submission: check -> fix -> grade.
    // Returns what was found, fixed and scored.
    public ArrayList<String> processSubmission(File zipFile)
            throws IOException {
        ArrayList<String> report = checkSubmission(zipFile);

        if (isChecked(zipFile)) {
            report.addAll(fixSubmission(zipFile));
            report.addAll(gradeSubmission(zipFile));
        }

        return report;
    }

    // Step 1. Check (Shanna, ShiYing): unzips the submission into a fresh
    // folder and finds what is wrong with it. Nothing is changed yet.
    public ArrayList<String> checkSubmission(File zipFile)
            throws IOException {
        ArrayList<String> report = new ArrayList<>();
        String username = UsernameResolver.getUsername(zipFile.getPath());

        if (username == null) {
            report.add(NO_USERNAME);
            return report;
        }

        File extractFolder = extractFolderOf(zipFile);
        FileHandlings.deleteFolder(extractFolder);
        ArrayList<Exception> found;

        try {
            FileHandlings.unzip(zipFile.getPath(), extractFolder.getPath());
            found = FileStructureChecker.structureCheck(TEMPLATE_ZIP,
                    zipFile.getPath());
        } catch (InvalidZipException exception) {
            // Empty or corrupt ZIP: nothing to check or fix, so it is
            // still graded (every question gets 0) and shows up in results
            found = new ArrayList<>();
            found.add(exception);
        }

        // A fresh unzip undoes any earlier fixes
        foundProblems.put(zipFile.getPath(), found);
        fixedSubmissions.remove(zipFile.getPath());

        if (found.isEmpty()) {
            report.add("No problems found.");
        }

        for (Exception problem : found) {
            report.add("Found " + describe(problem));
        }

        return report;
    }

    // Step 2. Fix (Marcus, Jinyan): repairs the problems the check found
    public ArrayList<String> fixSubmission(File zipFile) {
        ArrayList<String> report = new ArrayList<>();
        String username = UsernameResolver.getUsername(zipFile.getPath());

        if (username == null) {
            report.add(NO_USERNAME);
            return report;
        }

        if (!isChecked(zipFile)) {
            report.add(NOT_CHECKED);
            return report;
        }

        ArrayList<Exception> found = foundProblems.get(zipFile.getPath());

        if (found.isEmpty()) {
            report.add("Nothing to fix.");
        } else {
            File extractFolder = extractFolderOf(zipFile);
            ArrayList<String> fixes = SubmissionFixer.repairSubmission(
                    zipFile.getPath(), extractFolder.getPath(), found);

            if (fixes.isEmpty()) {
                report.add("Nothing could be fixed automatically.");
            }

            report.addAll(fixes);
        }

        fixedSubmissions.add(zipFile.getPath());
        return report;
    }

    // Step 3. Grade (Cedric): runs the testers on the fixed submission.
    // If there is still no username folder, every question gets 0.
    public ArrayList<String> gradeSubmission(File zipFile) {
        ArrayList<String> report = new ArrayList<>();
        String username = UsernameResolver.getUsername(zipFile.getPath());

        if (username == null) {
            report.add(NO_USERNAME);
            return report;
        }

        if (!isChecked(zipFile)) {
            report.add(NOT_CHECKED);
            return report;
        }

        if (!fixedSubmissions.contains(zipFile.getPath())) {
            report.add(NOT_FIXED);
            return report;
        }

        Student student = findStudent(username);
        File usernameFolder = new File(extractFolderOf(zipFile), username);
        StudentResult result;

        if (usernameFolder.isDirectory()) {
            result = Grading.gradePaper(student, usernameFolder,
                    testerFolder, timeLimitSeconds);
        } else {
            result = new StudentResult(student);
        }

        // Keep the result (replacing an older one for the same student)
        studentResults.removeIf(old -> old.getStudent().getUsername()
                .equals(username));
        studentResults.add(result);
        report.add(describeScores(result));

        return report;
    }

    private boolean isChecked(File zipFile) {
        return foundProblems.containsKey(zipFile.getPath());
    }

     public static File[] getJavaFiles(File directory) {
        ArrayList<File> javaFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files == null) {
            return new File[0];
        }

        for (File file : files) {
            if (file.isDirectory()) {
                javaFiles.addAll(
                        Arrays.asList(getJavaFiles(file)));
            } else if (file.getName().endsWith(".java")) {
                javaFiles.add(file);
            }
        }

        return javaFiles.toArray(new File[0]);
    }

    // e.g. unzippedFolder/2023-2024-ping.lee.2023
    private File extractFolderOf(File zipFile) {
        String zipName = zipFile.getName();
        int nameLength = zipName.length() - ZIP_EXTENSION.length();
        return new File(UNZIP_FOLDER, zipName.substring(0, nameLength));
    }

    // Looks the username up in the score sheet, e.g.
    // #01400001,#ping.lee.2023,_,PING LEE,ping.lee.2023@...,,20,#
    // If it is not there, only the username is filled in.
    private Student findStudent(String username) {
        try (Scanner sc = new Scanner(new File(scoreSheetFile))) {
            if (sc.hasNextLine()) {
                sc.nextLine(); // header
            }

            while (sc.hasNextLine()) {
                String[] fields = sc.nextLine().split(",", -1);

                if (fields.length >= 5
                        && clean(fields[1]).equals(username)) {
                    return new Student(clean(fields[0]), username,
                            clean(fields[2]), clean(fields[3]),
                            clean(fields[4]));
                }
            }
        } catch (FileNotFoundException exception) {
            // No score sheet: use the username only
        }

        return new Student("", username, "", "", "");
    }

    // Removes the "#" the score sheet puts in front of some values
    private static String clean(String value) {
        value = value.trim();
        return value.startsWith("#") ? value.substring(1) : value;
    }

    // e.g. "Score: Q1a 3.0/3.0, Q1b 3.0/3.0, ... Total 12.0/20.0"
    private String describeScores(StudentResult result) {
        TreeMap<String, QuestionResult> questions = new TreeMap<>(result.getQuestionResults());
        ArrayList<String> parts = new ArrayList<>();

        for (Map.Entry<String, QuestionResult> question : questions.entrySet()) {
            parts.add(question.getKey() + " "
                    + question.getValue().getScore() + "/"
                    + question.getValue().getMaxScore());
        }

        return "Score: " + String.join(", ", parts) + "  Total "
                + result.getTotalScore() + "/" + result.getMaxTotalScore();
    }

    // e.g. "[MissingFileException] Missing Q2/persons.txt"
    private String describe(Exception problem) {
        return "[" + problem.getClass().getSimpleName() + "] "
                + problem.getMessage();
    }

    public String getExportFile() {
        return EXPORT_FILE;
    }

    // Pairs of students who share unusual wrong answers, using the output
    // left by grading, so grade first. Highest score first.
    public ArrayList<SharedWrongOutputPair> findSharedWrongOutputs()
            throws IOException {
        StudentResult.loadStudentResultTemplate();

        return WrongOutputChecker.findSharedWrongOutputPairs(new File(UNZIP_FOLDER),
                new File(EXAM_TEMPLATE_FOLDER), testerFolder,
                timeLimitSeconds);
    }

    public ArrayList<ForeignHeader> findForeignHeaders() {
        
            File[] zipfiles = findZipFiles();
            if (zipfiles == null){
                return new ArrayList<>();
            }
            return HeaderChecker.findForeignHeaders(zipfiles);
    
    }

    public void viewResults() {
        GradingResultsViewer.viewResults(studentResults);
    }

    // The results table as text, for the GUI
    public String getResultsText() {
        return GradingResultsViewer.formatResults(studentResults);
    }

    public boolean hasResults() {
        return !studentResults.isEmpty();
    }

    // Returns false if the file could not be written
    public boolean exportCSV() {
        new File(EXPORT_FILE).getParentFile().mkdirs();
        return GradingResultsViewer.exportCSV(studentResults, EXPORT_FILE);
    }
}
