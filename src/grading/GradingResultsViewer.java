package grading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import model.StudentResult;
import model.Student;
import model.QuestionResult;

public class GradingResultsViewer {
    public static void viewResults(ArrayList<StudentResult> studentResults) {
        for (StudentResult studentResult : studentResults) {
            Student student = studentResult.getStudent();
            HashMap<String, QuestionResult> questionResults = studentResult.getQuestionResults();

            System.out.print("Student ID: " + student.getOrgDefinedId() + ", ");

            for (Map.Entry<String, QuestionResult> questionResult : questionResults.entrySet()) {
                System.out.println(questionResult.getKey() + ": " + questionResult.getValue().getScore() + ", ");
            }
        }
    }

    public static void exportCSV(ArrayList<StudentResult> studentResults, String scoreSheetFile) {
        try (PrintStream ps = new PrintStream(new File(scoreSheetFile))) {
            ps.println("OrgDefinedId,Username,Last Name,First Name,Email,Calculated Final Grade Numerator,Calculated Final Grade Denominator,End-of-Line Indicator");

            for (StudentResult studentResult : studentResults) {
                Student student = studentResult.getStudent();

                ps.println("#" + student.getOrgDefinedId() + ",#" + student.getUsername() +
                    "," + student.getLastName() + "," + student.getFirstName() + "," + student.getEmail() +
                    "," + studentResult.getTotalScore() + "," + studentResult.getMaxTotalScore() + ",#");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
