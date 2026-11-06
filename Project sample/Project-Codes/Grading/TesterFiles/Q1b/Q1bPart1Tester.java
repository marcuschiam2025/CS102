package TesterFiles.Q1b;

import java.util.*;
import TesterFiles.*;

public class Q1bPart1Tester extends Tester {
    public double grade() {
        Q1bPart1Tester q1bPart1Tester = new Q1bPart1Tester();

        ArrayList<Object> original = new ArrayList<Object>(Arrays.asList(2, 4));
        int expected = 6;
        q1bPart1Tester.test(expected, Q1b.getSumOfEvenIntegers(original), 1);

        return q1bPart1Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1bPart1Tester q1bPart1Tester = new Q1bPart1Tester();
        q1bPart1Tester.grade();
        // System.out.println(q1bPart1Tester.grade());
    }
}