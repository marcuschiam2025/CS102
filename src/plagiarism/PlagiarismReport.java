package plagiarism;

import java.util.ArrayList;

// Turns the plagiarism results into text, so the console menu and the
// GUI show the same report.
public class PlagiarismReport {
    // e.g. "1. chee.teo.2022 & copy.cat.2024 (score 5.01)
    //          Q2b case 2: expected,actual = ... (2 students gave this)"
    public static String formatSharedWrongOutputs(ArrayList<SharedWrongOutputPair> pairs) {
        if (pairs.isEmpty()) {
            return "No two students share an unusual wrong answer.\n";
        }

        StringBuilder report = new StringBuilder();
        report.append("These pairs gave the same wrong answers. A higher "
                + "score means more, or rarer, shared answers.\n");
        report.append("This is a reason to look closer, not proof.\n");

        for (int i = 0; i < pairs.size(); i++) {
            SharedWrongOutputPair pair = pairs.get(i);
            report.append(String.format("%n%d. %s & %s (score %.2f)%n",
                    i + 1, pair.getFirstUsername(),
                    pair.getSecondUsername(), pair.getScore()));

            for (SharedWrongOutput wrongOutput : pair.getSharedWrongOutputs()) {
                report.append("   " + wrongOutput.getQuestionId()
                        + " case " + wrongOutput.getCaseId()
                        + ": expected,actual = " + wrongOutput.getOutput()
                        + " (" + wrongOutput.getUsernames().size()
                        + " students gave this)\n");
            }
        }

        return report.toString();
    }

    // e.g. "copy.cat.2024's Q2/Q2b.java mentions chee.teo.2022:
    //        * Email ID: chee.teo.2022"
    public static String formatForeignHeaders(ArrayList<ForeignHeader> foreignHeaders) {
        if (foreignHeaders.isEmpty()) {
            return "No file mentions another student's username.\n";
        }

        StringBuilder report = new StringBuilder();
        report.append("Files that mention another student's username:\n");
        for (ForeignHeader fh : foreignHeaders) {
            report.append("   " + fh.getOwnerUsername() + "'s "
                    + fh.getFileName() + " mentions "
                    + fh.getFoundUsername() + ": " + fh.getLine() + "\n");

        }
        return report.toString();
    }
}
