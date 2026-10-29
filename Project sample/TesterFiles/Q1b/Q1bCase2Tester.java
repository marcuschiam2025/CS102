package Q1b;

import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q1bCase2Tester extends Tester {
    public double grade() {
        Q1bCase2Tester q1bCase2Tester = new Q1bCase2Tester();

        ArrayList<Object> original = new ArrayList<Object>(Arrays.asList(2, false, 4.0, "two"));
        int expected = 2;
        q1bCase2Tester.test(expected, Q1b.getSumOfEvenIntegers(original), 1);

        return q1bCase2Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1bCase2Tester q1bCase2Tester = new Q1bCase2Tester();
        q1bCase2Tester.grade();
        // System.out.println(q1bPart2Tester.grade());
    }
}