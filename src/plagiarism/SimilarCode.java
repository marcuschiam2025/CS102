package plagiarism;

// Two students whose copies of one file (e.g. Q2b.java) are very similar,
// after the code from the exam template is taken out.
public class SimilarCode {
    private String firstUsername;
    private String secondUsername;
    private String fileName;
    // From 0.0 (nothing in common) to 1.0 (the same)
    private double similarity;

    public SimilarCode(String firstUsername, String secondUsername,
            String fileName, double similarity) {
        this.firstUsername = firstUsername;
        this.secondUsername = secondUsername;
        this.fileName = fileName;
        this.similarity = similarity;
    }

    public String getFirstUsername() {
        return firstUsername;
    }

    public String getSecondUsername() {
        return secondUsername;
    }

    public String getFileName() {
        return fileName;
    }

    public double getSimilarity() {
        return similarity;
    }

    @Override
    public String toString() {
        return "SimilarCode [" + firstUsername + " & " + secondUsername
                + ", " + fileName + ", similarity=" + similarity + "]";
    }
}
