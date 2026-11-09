import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Tester;

public class Q3Part3Tester extends Tester {
    public double grade() {
        Q3Part3Tester q3Part3Tester = new Q3Part3Tester();

        ArrayList<Shape> inputs = new ArrayList<>();
        inputs.add(new Circle("C1", 500));
        inputs.add(new Circle("C2", 3));
        inputs.add(new Rectangle("R1", 10000, 2));
        inputs.add(new Rectangle("R2", 5, 3));
        String expected = "[C2=>28.0:19.0, R2=>15.0:16.0]";
        q3Part3Tester.test(expected, String.valueOf(Q3.sortShapes(inputs)), 1);

        return q3Part3Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q3Part3Tester q3Part3Tester = new Q3Part3Tester();
        q3Part3Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}