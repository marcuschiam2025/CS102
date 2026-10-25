// package TesterFiles;

import java.util.*;

public class Q1aTester extends Tester {
    public double grade() {
        Q1aTester q1aTester = new Q1aTester();
        // Thread ird = new Thread(new InfiniteRuntimeDetector());

        ArrayList<String> original = new ArrayList<String>(Arrays.asList("alice", "ben", "cathy"));
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList("alice", "ben", "cathy"));
        try {
            // ird.start();
            q1aTester.test(expected, Q1a.getIsogramWords(original), 1);
        } catch (Exception e) {
            catchException(e);
        } finally {
            // ird.interrupt();
        }

        original = new ArrayList<String>(Arrays.asList("ellen", "alan", "zoe"));
        expected = new ArrayList<String>(Arrays.asList("zoe"));
        try {
            q1aTester.test(expected, Q1a.getIsogramWords(original), 1);
        } catch (Exception e) {
            catchException(e);
        }

        original = new ArrayList<String>(Arrays.asList("Elle", "alan", "Daniel", "Canny"));
        expected = new ArrayList<String>(Arrays.asList("Daniel"));
        try {
            q1aTester.test(expected, Q1a.getIsogramWords(original), 1);
        } catch (Exception e) {
            catchException(e);
        }

        return q1aTester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1aTester q1aTester = new Q1aTester();
        System.out.println(q1aTester.grade());
    }
}
