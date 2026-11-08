import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Tester;

public class Q3Part1Tester extends Tester {
    public double grade() {
        Q3Part1Tester q3Part1Tester = new Q3Part1Tester();

        ArrayList<Shape> inputs = new ArrayList<>();
        inputs.add(new Rectangle("R1", 10, 2));
        inputs.add(new Rectangle("R2", 5, 3));
        inputs.add(new Rectangle("R3", 20, 3));
        inputs.add(new Rectangle("R4", 500, 3));
        String expected = "[R3=>60.0:46.0, R1=>20.0:24.0, R2=>15.0:16.0]";
        q3Part1Tester.test(expected, Q3.sortShapes(inputs), 1, true);

        return q3Part1Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q3Part1Tester q3Part1Tester = new Q3Part1Tester();
        q3Part1Tester.grade();
    }
}