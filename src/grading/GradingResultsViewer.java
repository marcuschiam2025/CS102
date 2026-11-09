package grading;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import model.StudentResult;
import model.Student;
import model.QuestionResult;

public class GradingResultsViewer {
    private static final String COLUMN = "%-8s";
    private static final String NAME_COLUMN = "%-16s";
    private static final String USERNAME_COLUMN = "%-17s";

    // Prints one row per student (sorted by username) with the score for
    // each question and the total, e.g.
    //   Username         Name            Q1a     Q1b   ...  Total
    //   ping.lee.2023    PING LEE        3/3     3/3   ...  20/20
    public static void viewResults(ArrayList<StudentResult> studentResults) {
        System.out.println();
        System.out.print(formatResults(studentResults));
    }

    // Builds the same table as viewResults as one String, one line per
    // student, so the GUI can show it too
    public static String formatResults(
            ArrayList<StudentResult> studentResults) {
        if (studentResults.isEmpty()) {
            return "No results yet. Grade the submissions first.\n";
        }

        StringBuilder table = new StringBuilder();
        ArrayList<StudentResult> sorted = new ArrayList<>(studentResults);
        sorted.sort(Comparator.comparing(
                result -> result.getStudent().getUsername()));

        // Every question that any student has, in order (Q1a, Q1b, ...)
        TreeSet<String> questionIds = new TreeSet<>();
        for (StudentResult studentResult : sorted) {
            questionIds.addAll(studentResult.getQuestionResults().keySet());
        }

        StringBuilder header = new StringBuilder();
        header.append(String.format(USERNAME_COLUMN, "Username"));
        header.append(String.format(NAME_COLUMN, "Name"));
        for (String questionId : questionIds) {
            header.append(String.format(COLUMN, questionId));
        }
        header.append("Total");

        table.append(header).append("\n");
        table.append("-".repeat(header.length())).append("\n");

        for (StudentResult studentResult : sorted) {
            Student student = studentResult.getStudent();
            HashMap<String, QuestionResult> questionResults = studentResult.getQuestionResults();
            StringBuilder row = new StringBuilder();

            row.append(String.format(USERNAME_COLUMN, student.getUsername()));
            row.append(String.format(NAME_COLUMN, student.getFirstName()));

            for (String questionId : questionIds) {
                QuestionResult questionResult = questionResults.get(questionId);
                String score = questionResult == null ? "-"
                        : formatScore(questionResult.getScore()) + "/"
                                + formatScore(questionResult.getMaxScore());
                row.append(String.format(COLUMN, score));
            }

            row.append(formatScore(studentResult.getTotalScore()) + "/"
                    + formatScore(studentResult.getMaxTotalScore()));
            table.append(row).append("\n");
        }

        return table.toString();
    }

    // 3.0 -> "3", 2.5 -> "2.5"
    private static String formatScore(double score) {
        if (score == Math.rint(score)) {
            return String.valueOf((long) score);
        }

        return String.valueOf(score);
    }

    // Returns true if the file was written, false if it could not be
    public static boolean exportCSV(ArrayList<StudentResult> studentResults, String scoreSheetFile) {
        try (PrintStream ps = new PrintStream(new File(scoreSheetFile))) {
            ps.println("OrgDefinedId,Username,Last Name,First Name,Email,Calculated Final Grade Numerator,Calculated Final Grade Denominator,End-of-Line Indicator");

            for (StudentResult studentResult : studentResults) {
                Student student = studentResult.getStudent();

                ps.println("#" + student.getOrgDefinedId() + ",#" + student.getUsername().trim() +"," + student.getLastName().trim() + "," + student.getFirstName().trim() + "," + student.getEmail().trim() +"," + studentResult.getTotalScore() + "," + studentResult.getMaxTotalScore() + ",#");
            }

            // PrintStream does not throw on write errors, so ask it
            return !ps.checkError();
        } catch (FileNotFoundException e) {
            // e.g. the file is open in Excel, or the folder is read-only
            return false;
        }
    }
}
