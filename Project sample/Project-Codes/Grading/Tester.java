// package TesterFiles;

import java.util.*;

public abstract class Tester {
    public double points;

    public abstract double grade();
    
    public void addPoints(double points) {
        this.points += points;
    }

    public void setPoints(double points) {
        this.points = points;
    }
    
    public double getPoints() {
        return points;
    }
    
    public void test(Object expected, Object actual, double points) {
        try {
            System.out.println("Expected: " + expected);
            System.out.println("Actual: " + actual);

            if (expected.equals(actual)) {
                System.out.println("Passed");
                addPoints(points);
            } else {
                System.out.println("Failed");
            }
        } catch (Exception e) {
            catchException(e);
        }
    }

    public void catchException(Exception e) {
        System.out.println("Exception caught!");
        e.printStackTrace();
    }
}
