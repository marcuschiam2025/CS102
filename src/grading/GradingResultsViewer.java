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

// Co-authored with Claude

public class GradingResultsViewer {
    private static final String COLUMN = "%-8s";
    private static final String NAME_COLUMN = "%-16s";
    private static final String USERNAME_COLUMN = "%-17s";
    // Same columns as the score sheet the school gives
    private static final String CSV_HEADER = "OrgDefinedId,Username,"
            + "Last Name,First Name,Email,"
            + "Calculated Final Grade Numerator,"
            + "Calculated Final Grade Denominator,End-of-Line Indicator";

    // Builds the results table, one row per student (sorted by username)
    // with the score for each question and the total, e.g.
    //   Username         Name            Q1a     Q1b   ...  Total
    //   ping.lee.2023    PING LEE        3/3     3/3   ...  20/20
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
            HashMap<String, QuestionResult> questionResults =
                    studentResult.getQuestionResults();
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

    // A value ready to put in a CSV file. A value with a comma, a quote or
    // a new line is put in quotes, and its quotes are doubled, e.g.
    // Lee, Ping -> "Lee, Ping" and say "hi" -> "say ""hi"""
    public static String csvField(String value) {
        if (value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    // Splits one CSV line into its values, the opposite of csvField, e.g.
    // #01,"Lee, Ping",x -> [#01, Lee, Ping, x]. Empty values are kept.
    public static ArrayList<String> splitCsvLine(String line) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"' && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    // "" inside quotes is one quote
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    inQuotes = false;
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }

        fields.add(field.toString());
        return fields;
    }

    // Returns true if the file was written, false if it could not be
    public static boolean exportCSV(ArrayList<StudentResult> studentResults,
            String scoreSheetFile) {
        try (PrintStream csvWriter =
                new PrintStream(new File(scoreSheetFile))) {
            csvWriter.println(CSV_HEADER);

            for (StudentResult studentResult : studentResults) {
                Student student = studentResult.getStudent();

                csvWriter.println(csvField("#" + student.getOrgDefinedId())
                        + "," + csvField("#" + student.getUsername().trim())
                        + "," + csvField(student.getLastName().trim())
                        + "," + csvField(student.getFirstName().trim())
                        + "," + csvField(student.getEmail().trim())
                        + "," + studentResult.getTotalScore()
                        + "," + studentResult.getMaxTotalScore()
                        + ",#");
            }

            // PrintStream does not throw on write errors, so ask it
            return !csvWriter.checkError();
        } catch (FileNotFoundException exception) {
            // e.g. the file is open in Excel, or the folder is read-only
            return false;
        }
    }
}
