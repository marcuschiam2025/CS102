// package TesterFiles;

import java.util.*;

public class Q1bTester extends Tester {
    public double grade() {
        Q1bTester q1bTester = new Q1bTester();

        ArrayList<Object> original = new ArrayList<Object>(Arrays.asList(2, 4));
        int expected = 6;
        q1bTester.test(expected, Q1b.getSumOfEvenIntegers(original), 1);
        
        original = new ArrayList<Object>(Arrays.asList(2, false, 4.0, "two"));
        expected = 2;
        q1bTester.test(expected, Q1b.getSumOfEvenIntegers(original), 1);

        original = new ArrayList<Object>(Arrays.asList(9, 2, 10, 3, 5, 8));
        expected = 20;
        q1bTester.test(expected, Q1b.getSumOfEvenIntegers(original), 1);

        return q1bTester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1bTester q1bTester = new Q1bTester();
        System.out.println(q1bTester.grade());
    }
}
