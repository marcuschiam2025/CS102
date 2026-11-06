package TesterFiles.Q1a;

import java.util.*;
import TesterFiles.*;

public class Q1aPart3Tester extends Tester {
    public double grade() {
        Q1aPart3Tester q1aPart3Tester = new Q1aPart3Tester();

        ArrayList<String> original = new ArrayList<String>(Arrays.asList("Elle", "alan", "Daniel", "Canny"));
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList("Daniel"));
        
        try {
            q1aPart3Tester.test(expected, Q1a.getIsogramWords(original), 1);
        } catch (Exception e) {
            catchException(e);
        }

        return q1aPart3Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1aPart3Tester q1aPart3Tester = new Q1aPart3Tester();
        q1aPart3Tester.grade();
        // System.out.println(q1aPart3Tester.grade());
    }
}
