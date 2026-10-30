package Q1b;

import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q1bCase3Tester extends Tester {
    public double grade() {
        Q1bCase3Tester q1bCase3Tester = new Q1bCase3Tester();

        ArrayList<Object> original = new ArrayList<Object>(Arrays.asList(9, 2, 10, 3, 5, 8));
        int expected = 20;
        q1bCase3Tester.test(expected, Q1b.getSumOfEvenIntegers(original), 1);

        return q1bCase3Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1bCase3Tester q1bCase3Tester = new Q1bCase3Tester();
        q1bCase3Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}