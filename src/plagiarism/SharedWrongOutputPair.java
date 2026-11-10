package plagiarism;

import java.util.ArrayList;

// Two students and the evidence against them. The score adds up the
// weight of everything they share, so a higher score means they share
// more answers, or rarer ones. It is a reason to look closer, not proof.
public class SuspiciousPair {
    private String firstUsername;
    private String secondUsername;
    private ArrayList<SharedWrongOutput> sharedWrongOutputs;

    public SuspiciousPair(String firstUsername, String secondUsername) {
        this.firstUsername = firstUsername;
        this.secondUsername = secondUsername;
        this.sharedWrongOutputs = new ArrayList<>();
    }

    public String getFirstUsername() {
        return firstUsername;
    }

    public String getSecondUsername() {
        return secondUsername;
    }

    public ArrayList<SharedWrongOutput> getSharedWrongOutputs() {
        return sharedWrongOutputs;
    }

    public void addSharedWrongOutput(SharedWrongOutput sharedWrongOutput) {
        sharedWrongOutputs.add(sharedWrongOutput);
    }

    public double getScore() {
        double score = 0.0;

        for (SharedWrongOutput sharedWrongOutput : sharedWrongOutputs) {
            score += sharedWrongOutput.getWeight();
        }

        return score;
    }

    @Override
    public String toString() {
        return "SuspiciousPair [" + firstUsername + " & " + secondUsername
                + ", score=" + getScore() + ", shared="
                + sharedWrongOutputs.size() + "]";
    }
}
