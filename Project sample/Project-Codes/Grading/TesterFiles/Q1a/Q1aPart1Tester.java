package TesterFiles.Q1a;

import java.util.*;
import TesterFiles.*;

public class Q1aPart1Tester extends Tester {
    public double grade() {
        Q1aPart1Tester q1aPart1Tester = new Q1aPart1Tester();

        ArrayList<String> original = new ArrayList<String>(Arrays.asList("alice", "ben", "cathy"));
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList("alice", "ben", "cathy"));
        
        try {
            q1aPart1Tester.test(expected, Q1a.getIsogramWords(original), 1);
        } catch (Exception e) {
            catchException(e);
        }

        return q1aPart1Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1aPart1Tester q1aPart1Tester = new Q1aPart1Tester();
        q1aPart1Tester.grade();
        // System.out.println(q1aPart1Tester.grade());
    }
}
