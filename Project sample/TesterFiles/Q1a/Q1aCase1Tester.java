package Q1a;

import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q1aCase1Tester extends Tester {
    public double grade() {
        Q1aCase1Tester q1aCase1Tester = new Q1aCase1Tester();

        ArrayList<String> original = new ArrayList<String>(Arrays.asList("alice", "ben", "cathy"));
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList("alice", "ben", "cathy"));
        
        try {
            q1aCase1Tester.test(expected, Q1a.getIsogramWords(original), 1);
        } catch (Exception e) {
            catchException(e);
        }

        return q1aCase1Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1aCase1Tester q1aCase1Tester = new Q1aCase1Tester();
        q1aCase1Tester.grade();
        // System.out.println(q1aPart1Tester.grade());
    }
}
