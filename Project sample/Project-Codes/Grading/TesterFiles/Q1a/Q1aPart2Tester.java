package TesterFiles.Q1a;

import java.util.*;
import TesterFiles.*;

public class Q1aPart2Tester extends Tester {
    public double grade() {
        Q1aPart2Tester q1aPart2Tester = new Q1aPart2Tester();

        ArrayList<String> original = new ArrayList<String>(Arrays.asList("ellen", "alan", "zoe"));
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList("zoe"));
        
        try {
            q1aPart2Tester.test(expected, Q1a.getIsogramWords(original), 1);
        } catch (Exception e) {
            catchException(e);
        }

        return q1aPart2Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1aPart2Tester q1aPart2Tester = new Q1aPart2Tester();
        q1aPart2Tester.grade();
        // System.out.println(q1aPart2Tester.grade());
    }
}
