package model;

// import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

// All question scores for one student, plus the total
public class StudentResult {
    private Student student;
    private HashMap<String, QuestionResult> questionResults = new HashMap<String, QuestionResult>();
    // suggestion:
    // GradingResults for StudentResult
    // question data (max score) can be put into another java class
    // (its not possible to detemine the max score if a compilation error occurs)
    
    // i combined GradingResult with QuestionResult's status enum and max marks
    // should we keep part result???
    // max marks and question number must be put into a template class
    // grading result takes an instance of the template class and uses it to mark

    // template map for question results
    public static String questionResultTemplateFilePath = "src/model/QuestionResultTemplate.csv";
    public static HashMap<String, QuestionResult> questionResultTemplate = new HashMap<String, QuestionResult>();

    public StudentResult(Student student) {
        this.student = student;

        // prevent looking at the same HashMap address
        for (Map.Entry<String, QuestionResult> questionResult : questionResultTemplate.entrySet()) {
            questionResults.put(questionResult.getKey(), questionResult.getValue().clone());
        }
    }

    public Student getStudent() {
        return student;
    }

    public HashMap<String, QuestionResult> getQuestionResults() {
        return questionResults;
    }

    public void addQuestionResult(String questionId, QuestionResult questionResult) {
        questionResults.put(questionId, questionResult);
    }

    public void addQuestionResult(StudentResult studentResult) {
        for (Map.Entry<String, QuestionResult> questionResult : studentResult.getQuestionResults().entrySet()) {
            for (Map.Entry<String, CaseResult> caseResult : questionResult.getValue().getCases().entrySet()) {
                CaseResult caseResultOriginal = this.questionResults.get(questionResult.getKey()).getCaseResult(caseResult.getKey());
                CaseResult caseResultCopy = caseResult.getValue();
                caseResultOriginal.setCaseResult(caseResultCopy.getScore(), caseResultCopy.getStatus(), caseResultCopy.getDetails());
            }
        }
    }

    public void addCaseResult(String questionId, String caseId, double score, CaseResultStatus status, String details) {
        this.questionResults.get(questionId).getCaseResult(caseId).setCaseResult(score, status, details);
    }

    // Returns the result for one question, e.g. "Q1a",
    // or null if it has not been graded
    public QuestionResult getQuestionResult(String questionId) {
        return questionResults.get(questionId);
    }

    public double getTotalScore() {
        double total = 0;
        for (QuestionResult result : questionResults.values()) {
            total += result.getScore();
        }
        return total;
    }

    public double getMaxTotalScore() {
        double total = 0;
        for (QuestionResult result : questionResults.values()) {
            total += result.getMaxScore();
        }
        return total;
    }

    public HashMap<String, ArrayList<String>> getQuestionAndCaseIds() {
        HashMap<String, ArrayList<String>> questionIds = new HashMap<String, ArrayList<String>>();

        for (Map.Entry<String, QuestionResult> questionResult : questionResults.entrySet()) {
            questionIds.put(questionResult.getKey(), questionResult.getValue().getCaseIds());
        }

        return questionIds;
    }

    public static void loadQuestionResultTemplate() {
        try (Scanner sc = new Scanner(new File(questionResultTemplateFilePath))) {
            sc.nextLine();

            while (sc.hasNext()) {
                String[] caseData = sc.nextLine().split(",");
                String questionId = caseData[0].trim();
                String caseId = caseData[1].trim();
                double maxScore = Double.parseDouble(caseData[2].trim());

                if (questionResultTemplate.containsKey(questionId)) {
                    questionResultTemplate.get(questionId).addCaseResult(caseId, maxScore);
                } else {
                    questionResultTemplate.put(questionId, new QuestionResult(caseId, maxScore));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "StudentResult [username=" + student.getUsername()
                + ", totalScore=" + getTotalScore()
                + "/" + getMaxTotalScore() + "]";
    }

    // main program to test the template;
    public static void main(String[] args) {
        loadQuestionResultTemplate();
        System.out.println(questionResultTemplate);
        Student test = new Student("a", "a", "a", "a", "a");
        StudentResult testResult = new StudentResult(test);
        testResult.getQuestionResult("3").getCaseResult("4").setCaseResult(1.0, CaseResultStatus.PASSED);
        System.out.println(testResult.getTotalScore() == new StudentResult(test).getTotalScore());
    }
}
