package model;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
// import java.util.Objects;

class CaseResult {
    // Possible values for status
    // public static final String COMPLETED = "Completed";
    // killed for running too long, score is the passed cases only
    // public static final String TIMEOUT = "Timeout";
    // public static final String COMPILE_ERROR = "Compile error";
    // public static final String MISSING_FILE = "Missing file";

    // private String caseId;
    private double score;
    private double maxScore;
    private CaseResultStatus status;
    // e.g. the compile error message, empty if there is nothing to add
    private String details;

    public CaseResult(double score, double maxScore,
            CaseResultStatus status, String details) {
        // this.caseId = caseId;
        this.score = score;
        this.maxScore = maxScore;
        this.status = status;
        this.details = details;
    }

    public CaseResult(double score, double maxScore,
            CaseResultStatus status) {
        // this.caseId = caseId;
        this(score, maxScore, status, "");
    }

    public CaseResult(double maxScore) {
        this(0.0, maxScore, CaseResultStatus.MISSING_FILE, "");
    }

    // public String getCaseId() {
    //     return caseId;
    // }

    public double getScore() {
        return score;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public CaseResultStatus getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }

    public void setCaseResult(double score, CaseResultStatus status, String details) {
        this.score = score;
        this.status = status;
        this.details = details;
    }

    public void setCaseResult(double score, CaseResultStatus status) {
        this.setCaseResult(score, status, "");
    }

    @Override
    public String toString() {
        return "CaseResult [score=" + score + ", maxScore=" + maxScore
                + ", status=" + status + "]";
    }
}

// The score for one question, e.g. Q1a: 2.0 / 3.0
public class QuestionResult implements Cloneable {
    // private String questionId;
    // questions are split into cases for more analysis
    private HashMap<String, CaseResult> cases = new HashMap<String, CaseResult>();

    // public QuestionResult(String questionId) {
    //     this.questionId = questionId;
    // }

    public QuestionResult(String caseId, double score, double maxScore,
            CaseResultStatus status, String details) {
        // this(questionId);
        cases.put(caseId, new CaseResult(score, maxScore, status, details));
    }

    public QuestionResult(String caseId, double score, double maxScore,
            CaseResultStatus status) {
        // this(questionId);
        cases.put(caseId, new CaseResult(score, maxScore, status));
    }

    public QuestionResult(String caseId, double maxScore) {
        // this(questionId);
        cases.put(caseId, new CaseResult(maxScore));
    }

    public QuestionResult() {}

    // public String getQuestionId() {
    //     return questionId;
    // }

    public CaseResult getCaseResult(String caseId) {
        return cases.get(caseId);
    }
    
    public HashMap<String, CaseResult> getCases() {
        return cases;
    }

    public double getScore() {
        double score = 0.0;

        for (CaseResult caseResult : cases.values()) {
            score += caseResult.getScore();
        }

        return score;
    }

    public double getMaxScore() {
        double maxScore = 0.0;

        for (CaseResult caseResult : cases.values()) {
            maxScore += caseResult.getScore();
        }

        return maxScore;
    }

    public ArrayList<String> getCaseIds() {
        ArrayList<String> caseIds = new ArrayList<String>();

        for (String caseId : cases.keySet()) {
            caseIds.add(caseId);
        }

        return caseIds;
    }

    public void addCaseResult(String caseId, double maxScore) {
        cases.put(caseId, new CaseResult(maxScore));
    }

    @Override
    public String toString() {
        return "QuestionResult [cases=" + cases + "]";
    }

    @Override
    public QuestionResult clone() {
        QuestionResult questionResultNew = new QuestionResult();

        for (Map.Entry<String, CaseResult> caseResult : cases.entrySet()) {
            questionResultNew.addCaseResult(caseResult.getKey().toString(), Double.valueOf(caseResult.getValue().getMaxScore()));
        }
        
        return questionResultNew;
    }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(questionId);
    // }
}
