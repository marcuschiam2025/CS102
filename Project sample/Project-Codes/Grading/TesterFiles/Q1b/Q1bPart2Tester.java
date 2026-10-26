package TesterFiles.Q1b;

import java.util.*;
import TesterFiles.*;

public class Q1bPart2Tester extends Tester {
    public double grade() {
        Q1bPart2Tester q1bPart2Tester = new Q1bPart2Tester();

        ArrayList<Object> original = new ArrayList<Object>(Arrays.asList(2, false, 4.0, "two"));
        int expected = 2;
        q1bPart2Tester.test(expected, Q1b.getSumOfEvenIntegers(original), 1);

        return q1bPart2Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1bPart2Tester q1bPart2Tester = new Q1bPart2Tester();
        q1bPart2Tester.grade();
        // System.out.println(q1bPart2Tester.grade());
    }
}