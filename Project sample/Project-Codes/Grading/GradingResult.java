import java.util.*;

class PartResult {
    private String expected;
    private String actual;
    private boolean passed;
    private double marks;

    public PartResult(String expected, String actual, boolean passed, double marks) {
        this.expected = expected;
        this.actual = actual;
        this.passed = passed;
        this.marks = marks;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getExpected() {
        return expected;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getActual() {
        return actual;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean getPassed() {
        return passed;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    public double getMarks() {
        return marks;
    }

    @Override
    public String toString() {
        return "{expected=" + expected + ",actual=" + actual + ",passed=" + passed + ",marks=" + marks + "}";
    }
}

class QuestionResult {
    private HashMap<String, PartResult> partResults = new HashMap<String, PartResult>();

    public void addPartResult(String name, String expected, String actual, boolean passed, double marks) {
        partResults.put(name, new PartResult(expected, actual, passed, marks));
    }

    public void addPartResult(String name, PartResult partResult) {
        partResults.put(name, partResult);
    }

    public void addPartResult(QuestionResult questionResult) {
        for (Map.Entry<String, PartResult> partResult : questionResult.getPartResults().entrySet()) {
            String partName = partResult.getKey();

            if (!partResults.containsKey(partName)) {
                addPartResult(partName, partResult.getValue());
            }
        }
    }

    public HashMap<String, PartResult> getPartResults() {
        return partResults;
    }

    public PartResult getPartResult(String name) {
        return partResults.get(name);
    }

    public double getTotalMarks() {
        double totalMarks = 0.0;

        for (PartResult partResult : partResults.values()) {
            totalMarks += partResult.getMarks();
        }

        return totalMarks;
    }

    @Override
    public String toString() {
        return partResults.toString();
    }
}

public class GradingResult {
    private HashMap<String, QuestionResult> questionResults = new HashMap<String, QuestionResult>();

    public void addQuestionResult(String name) {
        questionResults.put(name, new QuestionResult());
    }

    public void addPartResult(String questionName, String partName, String expected, String actual, boolean passed, double marks) {
        if (!questionResults.containsKey(questionName)) {
            questionResults.put(questionName, new QuestionResult());
        }

        questionResults.get(questionName).addPartResult(partName, new PartResult(expected, actual, passed, marks));
    }

    public void addQuestionResult(GradingResult gradingResult) {
        for (Map.Entry<String, QuestionResult> questionResult : gradingResult.getQuestionResults().entrySet()) {
            String questionName = questionResult.getKey();

            if (!questionResults.containsKey(questionName)) {
                this.addQuestionResult(questionName);
            }

            QuestionResult questionResult2 = this.getQuestionResult(questionName);
            questionResult2.addPartResult(questionResult.getValue());
        }
    }

    public HashMap<String, QuestionResult> getQuestionResults() {
        return questionResults;
    }

    public QuestionResult getQuestionResult(String name) {
        return questionResults.get(name);
    }

    public double getTotalMarks() {
        double totalMarks = 0.0;

        for (QuestionResult questionResult : questionResults.values()) {
            totalMarks += questionResult.getTotalMarks();
        }

        return totalMarks;
    }

    @Override
    public String toString() {
        return questionResults.toString();
    }
}