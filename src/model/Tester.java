package model;

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

    public void printResultBase() {
        String testerName = this.getClass().getSimpleName();

        // e.g. "Q1aPart2Tester" prints "Q1a,2,"
        String questionId = testerName.replaceAll("(?!Q)[A-Z].+Tester$", "");
        String caseId =
                testerName.replaceAll("^Q.+Part(?=.+Tester)|Tester", "");

        System.out.print(questionId + ",");
        System.out.print(caseId + ",");
    }

    public void test(Object expected, Object actual, double points,
            boolean useToString) {
        try {
            printResultBase();

            System.out.print(expected + ",");
            System.out.print(actual + ",");

            if (expected.equals((useToString) ? actual.toString() : actual)) {
                System.out.println("true," + points);
                addPoints(points);
            } else {
                System.out.println("false,0.0");
            }
        } catch (Exception exception) {
            catchException(exception);
        }
    }

    public void test(Object expected, Object actual, double points) {
        test(expected, actual, points, false);
    }

    public void printResult(Object expected, Object actual, boolean passed,
            double points) {
        try {
            printResultBase();

            System.out.print(expected + ",");
            System.out.print(actual + ",");
            addPoints(points);
            System.out.print(passed + ",");
            System.out.println(points);
        } catch (Exception exception) {
            catchException(exception);
        }
    }

    public void catchException(Exception exception) {
        printResultBase();
        System.out.println("-,(Runtime Error),false,0.0");
    }
}
