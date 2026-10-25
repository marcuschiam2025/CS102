package TesterFiles.Q1b;

import java.util.*;
import TesterFiles.*;

public class Q1bPart3Tester extends Tester {
    public double grade() {
        Q1bPart3Tester q1bPart3Tester = new Q1bPart3Tester();

        ArrayList<Object> original = new ArrayList<Object>(Arrays.asList(9, 2, 10, 3, 5, 8));
        int expected = 20;
        q1bPart3Tester.test(expected, Q1b.getSumOfEvenIntegers(original), 1);

        return q1bPart3Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1bPart3Tester q1bPart3Tester = new Q1bPart3Tester();
        q1bPart3Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}