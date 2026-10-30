package Q1b;

import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q1bCase1Tester extends Tester {
    public double grade() {
        Q1bCase1Tester q1bCase1Tester = new Q1bCase1Tester();

        ArrayList<Object> original = new ArrayList<Object>(Arrays.asList(2, 4));
        int expected = 6;
        q1bCase1Tester.test(expected, Q1b.getSumOfEvenIntegers(original), 1);

        return q1bCase1Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q1bCase1Tester q1bCase1Tester = new Q1bCase1Tester();
        q1bCase1Tester.grade();
        // System.out.println(q1bPart1Tester.grade());
    }
}