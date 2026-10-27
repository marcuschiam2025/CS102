package model;

// The score for one question, e.g. Q1a: 2.0 / 3.0
public class QuestionResult {
    // Possible values for status
    public static final String COMPLETED = "Completed";
    // killed for running too long, score is the passed cases only
    public static final String TIMEOUT = "Timeout";
    public static final String COMPILE_ERROR = "Compile error";
    public static final String MISSING_FILE = "Missing file";

    private String questionId;
    private double score;
    private double maxScore;
    private String status;
    // e.g. the compile error message, empty if there is nothing to add
    private String details;

    public QuestionResult(String questionId, double score, double maxScore,
            String status, String details) {
        this.questionId = questionId;
        this.score = score;
        this.maxScore = maxScore;
        this.status = status;
        this.details = details;
    }

    public String getQuestionId() {
        return questionId;
    }

    public double getScore() {
        return score;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public String getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "QuestionResult [questionId=" + questionId
                + ", score=" + score + ", maxScore=" + maxScore
                + ", status=" + status + "]";
    }
}
