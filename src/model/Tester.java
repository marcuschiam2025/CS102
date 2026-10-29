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
    
    public void test(Object expected, Object actual, double points) {
        try {
            String testerName = this.getClass().getSimpleName();
            
            System.out.print(testerName.replaceAll("(?!Q)[A-Z].+Tester$", "") + ",");
            System.out.print(testerName.replaceAll("^Q.+(?=[A-Z].+Tester)|Tester", "") + ",");
            System.out.print(expected + ",");
            System.out.print(actual + ",");

            if (expected.equals(actual)) {
                System.out.println("true," + points);
                addPoints(points);
            } else {
                System.out.println("false,0.0");
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
