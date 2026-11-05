package Q1a;

import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q1aCase2Tester extends Tester {
    public double grade() {
        Q1aCase2Tester q1aCase2Tester = new Q1aCase2Tester();

        ArrayList<String> original = new ArrayList<String>(Arrays.asList("ellen", "alan", "zoe"));
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList("zoe"));
        
        try {
            q1aCase2Tester.test(expected, Q1a.getIsogramWords(original), 1);
        } catch (Exception e) {
            catchException(e);
        }

        return q1aCase2Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1aCase2Tester q1aCase2Tester = new Q1aCase2Tester();
        q1aCase2Tester.grade();
        // System.out.println(q1aPart2Tester.grade());
    }
}
