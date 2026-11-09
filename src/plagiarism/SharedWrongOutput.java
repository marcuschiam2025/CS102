package plagiarism;

import java.util.ArrayList;

// One wrong answer to one test case, and every student who gave it,
// e.g. Q2b case 3 answered "Jason LEE-4.0" by chee.teo.2022
public class SharedWrongOutput {
    private String questionId;
    private String caseId;
    // As the tester printed it: "expected,actual", e.g. "6,-1"
    private String output;
    private ArrayList<String> usernames;
    // How unusual this answer is, see WrongOutputChecker
    private double weight;

    public SharedWrongOutput(String questionId, String caseId,
            String output) {
        this.questionId = questionId;
        this.caseId = caseId;
        this.output = output;
        this.usernames = new ArrayList<>();
        this.weight = 0.0;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getOutput() {
        return output;
    }

    public ArrayList<String> getUsernames() {
        return usernames;
    }

    public void addUsername(String username) {
        usernames.add(username);
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "SharedWrongOutput [" + questionId + " case " + caseId
                + ", output=" + output + ", usernames=" + usernames
                + ", weight=" + weight + "]";
    }
}
