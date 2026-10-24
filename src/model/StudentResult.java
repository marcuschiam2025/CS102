package model;

import java.util.ArrayList;

// All question scores for one student, plus the total
public class StudentResult {
    private Student student;
    private ArrayList<QuestionResult> questionResults;

    public StudentResult(Student student) {
        this.student = student;
        this.questionResults = new ArrayList<>();
    }

    public Student getStudent() {
        return student;
    }

    public ArrayList<QuestionResult> getQuestionResults() {
        return questionResults;
    }

    public void addQuestionResult(QuestionResult questionResult) {
        questionResults.add(questionResult);
    }

    // Returns the result for one question, e.g. "Q1a",
    // or null if it has not been graded
    public QuestionResult getQuestionResult(String questionId) {
        for (QuestionResult result : questionResults) {
            if (result.getQuestionId().equals(questionId)) {
                return result;
            }
        }
        return null;
    }

    public double getTotalScore() {
        double total = 0;
        for (QuestionResult result : questionResults) {
            total += result.getScore();
        }
        return total;
    }

    public double getMaxTotalScore() {
        double total = 0;
        for (QuestionResult result : questionResults) {
            total += result.getMaxScore();
        }
        return total;
    }

    @Override
    public String toString() {
        return "StudentResult [username=" + student.getUsername()
                + ", totalScore=" + getTotalScore()
                + "/" + getMaxTotalScore() + "]";
    }
}
